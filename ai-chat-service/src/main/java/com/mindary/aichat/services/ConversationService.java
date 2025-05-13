package com.mindary.aichat.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mindary.aichat.models.ChatMessage;
import com.mindary.aichat.models.Conversation;
import com.mindary.aichat.models.ConversationStatus;
import com.mindary.aichat.models.FollowUpAnalysis;
import com.mindary.aichat.models.MessageType;
import com.mindary.aichat.repositories.ChatMessageRepository;
import com.mindary.aichat.repositories.ConversationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final EmbeddingService embeddingService;
    private final GeminiService geminiService;
    private final DiaryAnalysisService diaryAnalysisService;

    public Conversation createConversation(UUID userId, String initialMessage) {
        String diaryInsight = diaryAnalysisService.getLatestAnalysisSummary(userId.toString());

        String aiResponse = geminiService.generateResponse(initialMessage, null, diaryInsight, "therapist");

        // create and save conversation
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(geminiService.generateConversationTitle(initialMessage));
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setLastMessage(initialMessage);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation = conversationRepository.save(conversation);

        // create and save initial chat message
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversation.getId());
        chatMessage.setUserId(userId);
        chatMessage.setType(MessageType.USER);
        chatMessage.setMessage(initialMessage);
        chatMessage.setResponse(aiResponse);
        chatMessage.setTimestamp(LocalDateTime.now());
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // Create embedding for the message
        embeddingService.createEmbedding(
                savedMessage.getId(),
                conversation.getId(),
                initialMessage + "\n" + aiResponse
        );

        // analyze for potential follow-up
        analyzeAndScheduleFollowUp(conversation.getId(), initialMessage);

        return conversation;
    }

    private void analyzeAndScheduleFollowUp(String conversationId, String message) {
        FollowUpAnalysis analysis = geminiService.analyzeForFollowUp(message);
        if (analysis.isNeedsFollowUp()) {
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation != null) {
                conversation.setFollowUpType(analysis.getFollowUpType());
                conversation.setFollowUpDue(LocalDateTime.now().plusHours(analysis.getFollowUpHours()));
                conversation.setFollowedUp(false);
                conversationRepository.save(conversation);
            }
        }
    }

    public List<ChatMessage> getConversationHistory(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    public List<Conversation> getUserConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);
    }

    public Conversation getConversation(String conversationId) {
        try {
            log.info("Fetching conversation: {}", conversationId);
            return conversationRepository.findById(conversationId)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error fetching conversation {}: {}", conversationId, e.getMessage());
            return null;
        }
    }

    public String deleteConversation(String conversationId) {
        try {
            log.info("Attempting to delete conversation: {}", conversationId);
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null) {
                log.warn("Conversation not found: {}", conversationId);
                return null;
            }

            String title = conversation.getTitle();
            chatMessageRepository.deleteByConversationId(conversationId);
            conversationRepository.deleteById(conversationId);
            log.info("Successfully deleted conversation: {} ({})", title, conversationId);
            return title;
        } catch (Exception e) {
            log.error("Failed to delete conversation {}: {}", conversationId, e.getMessage());
            return null;
        }
    }

    public void deleteMessage(String conversationId, String messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Verify message belongs to conversation
        if (!message.getConversationId().equals(conversationId)) {
            throw new RuntimeException("Message does not belong to this conversation");
        }

        chatMessageRepository.deleteById(messageId);
    }

    public List<Map<String, Object>> getUserConversationSummaries(UUID userId) {
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);

        return conversations.stream().map(conv -> {
            Map<String, Object> summary = new HashMap<>();
            summary.put("id", conv.getId());
            summary.put("title", conv.getTitle());
            summary.put("lastMessage", conv.getLastMessage());
            summary.put("lastMessageAt", conv.getLastMessageAt());
            summary.put("status", conv.getStatus());

            // Get message count
            long messageCount = chatMessageRepository.countByConversationId(conv.getId());
            summary.put("messageCount", messageCount);

            return summary;
        }).toList();
    }

    public ChatMessage saveMessage(String conversationId, UUID userId, String message, String response, String mode) {
        // Get diary analysis for context
        String diaryInsight = diaryAnalysisService.getLatestAnalysisSummary(userId.toString());

        // Generate AI response with diary context
        String aiResponse = geminiService.generateResponse(message, conversationId, diaryInsight, mode == null ? "therapist" : mode);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversationId);
        chatMessage.setUserId(userId);
        chatMessage.setType(MessageType.USER);
        chatMessage.setMessage(message);
        chatMessage.setResponse(aiResponse);
        chatMessage.setTimestamp(LocalDateTime.now());
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // Create embedding for the message
        embeddingService.createEmbedding(
                savedMessage.getId(),
                conversationId,
                message + "\n" + aiResponse
        );

        // Background: ask Gemini for follow-up suggestion
        new Thread(() -> {
            try {
                GeminiService.FollowUpSuggestion suggestion = geminiService.getFollowUpSuggestion(message, diaryInsight, mode);
                if (suggestion != null) {
                    Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
                    if (conversation != null) {
                        conversation.setFollowUpActive(true);
                        conversation.setFollowUpCount(0); // reset on new suggestion
                        conversation.setFollowUpDue(suggestion.due);
                        conversation.setFollowUpPrompt(suggestion.prompt);
                        conversation.setFollowUpReason(suggestion.reason);
                        conversationRepository.save(conversation);
                    }
                } else {
                    Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
                    if (conversation != null) {
                        conversation.setFollowUpActive(false);
                        conversation.setFollowUpPrompt(null);
                        conversation.setFollowUpReason(null);
                        conversationRepository.save(conversation);
                    }
                }
            } catch (Exception e) {
                log.error("Error in background follow-up suggestion", e);
            }
        }).start();

        return savedMessage;
    }

    public String getRelevantContext(String conversationId, String currentMessage) {
        List<String> similarMessages = embeddingService.findSimilarMessages(
                conversationId,
                currentMessage,
                5
        );
        return String.join("\n\n", similarMessages);
    }

    public Conversation updateConversationTitle(String conversationId, String newTitle) {
        try {
            log.info("Updating title for conversation: {} to: {}", conversationId, newTitle);
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            conversation.setTitle(newTitle);
            return conversationRepository.save(conversation);
        } catch (Exception e) {
            log.error("Error updating conversation title: {}", e.getMessage());
            throw new RuntimeException("Failed to update conversation title", e);
        }
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void processDueFollowUps() {
        List<Conversation> dueConvs = conversationRepository.findAll().stream()
                .filter(conv -> conv.isFollowUpActive()
                && conv.getFollowUpDue() != null
                && conv.getFollowUpDue().isBefore(LocalDateTime.now())
                && conv.getFollowUpCount() < 3)
                .toList();
        for (Conversation conv : dueConvs) {
            // Check if user has replied since last AI message
            List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByTimestampAsc(conv.getId());
            boolean userReplied = false;
            for (int i = messages.size() - 1; i >= 0; i--) {
                ChatMessage msg = messages.get(i);
                if (msg.getType() == MessageType.AI && msg.getMessage().equals(conv.getFollowUpPrompt())) {
                    break;
                }
                if (msg.getType() == MessageType.USER) {
                    userReplied = true;
                    break;
                }
            }
            if (userReplied) {
                conv.setFollowUpActive(false);
                conversationRepository.save(conv);
                continue;
            }

            ChatMessage followUp = new ChatMessage();
            followUp.setConversationId(conv.getId());
            followUp.setUserId(conv.getUserId());
            followUp.setType(MessageType.AI);
            followUp.setMessage(conv.getFollowUpPrompt());
            followUp.setResponse(null);
            followUp.setTimestamp(LocalDateTime.now());
            chatMessageRepository.save(followUp);

            conv.setFollowUpCount(conv.getFollowUpCount() + 1);
            if (conv.getFollowUpCount() >= 3) {
                conv.setFollowUpActive(false);
            } else {
                // Space out next follow-up: +3 days, +1 week, etc.
                int days = conv.getFollowUpCount() == 1 ? 3 : 7;
                conv.setFollowUpDue(LocalDateTime.now().plusDays(days));
            }
            conversationRepository.save(conv);
        }
    }
}
