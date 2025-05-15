package com.mindary.aichat.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;
    private UUID userId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private ConversationStatus status;
    private FollowUpType followUpType;
    private LocalDateTime followUpDue;
    private boolean isFollowedUp;

    // Follow-up enhancements
    private int followUpCount; // How many follow-ups have been sent
    private String followUpReason; // e.g., "test", "bad day", etc.
    private String followUpPrompt; // What to say in the follow-up
    private boolean followUpActive; // Is a follow-up scheduled?
}
