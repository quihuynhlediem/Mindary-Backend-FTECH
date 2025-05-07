package com.mindary.aichat.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mindary.aichat.dto.amqp.DiaryAnalysisDto;
import com.mindary.aichat.models.UserDiaryInsight;
import com.mindary.aichat.repositories.UserDiaryInsightRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDiaryInsightService {

    private final UserDiaryInsightRepository repository;

    /**
     * Stores or updates the latest diary analysis insight for a user. Uses
     * userId as the key for upserting.
     */
    public void storeLatestInsight(UUID userId, DiaryAnalysisDto insightDto) {
        if (userId == null || insightDto == null) {
            log.warn("Cannot store insight with null userId or insightDto");
            return;
        }

        // Try to find existing record
        Optional<UserDiaryInsight> existingInsightOpt = repository.findByUserId(userId);

        UserDiaryInsight insightToSave;
        if (existingInsightOpt.isPresent()) {
            // Update existing record
            insightToSave = existingInsightOpt.get();
            insightToSave.setLatestAnalysis(insightDto);
            insightToSave.setLastUpdatedAt(LocalDateTime.now());
            log.info("Updating existing diary insight for user: {}", userId);
        } else {
            // Create new record
            insightToSave = new UserDiaryInsight(userId, insightDto);
            log.info("Storing new diary insight for user: {}", userId);
        }

        try {
            repository.save(insightToSave); // save() performs upsert based on @Id or indexed field
        } catch (Exception e) {
            log.error("Error saving UserDiaryInsight for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Retrieves the latest stored diary analysis insight for a user. Returns
     * null if no insight is found.
     */
    public DiaryAnalysisDto getLatestInsight(UUID userId) {
        if (userId == null) {
            return null;
        }
        try {
            Optional<UserDiaryInsight> insightOpt = repository.findByUserId(userId);
            return insightOpt.map(UserDiaryInsight::getLatestAnalysis).orElse(null);
        } catch (Exception e) {
            log.error("Error retrieving UserDiaryInsight for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Formats the DiaryAnalysisDto into a concise string for the LLM prompt.
     */
    public String formatInsightForPrompt(DiaryAnalysisDto insight) {
        if (insight == null) {
            return null;
        }
        // Simple formatting, adjust as needed for better LLM consumption
        StringBuilder sb = new StringBuilder("\n--- Recent Diary Analysis Insight ---\n");
        if (insight.getEmotion() != null && insight.getEmotion().getName() != null) {
            sb.append("Overall Emotion: ").append(insight.getEmotion().getName());
            if (insight.getEmotion().getDescription() != null) {
                sb.append(" (").append(insight.getEmotion().getDescription()).append(")");
            }
            sb.append(".\n");
        }
        if (insight.getSymptoms() != null && !insight.getSymptoms().isEmpty()) {
            sb.append("Mentioned Symptoms: ");
            insight.getSymptoms().stream()
                    .map(s -> s.getName() != null ? s.getName() : "Unknown")
                    .limit(3) // Limit for prompt brevity
                    .forEach(name -> sb.append(name).append(", "));
            sb.setLength(sb.length() - 2); // Remove trailing ", "
            sb.append(".\n");
        }
        if (insight.getCorrelations() != null && !insight.getCorrelations().isEmpty()) {
            sb.append("Possible Correlations: ");
            insight.getCorrelations().stream()
                    .map(c -> c.getName() != null ? c.getName() : "Unknown")
                    .limit(2)
                    .forEach(name -> sb.append(name).append(", "));
            sb.setLength(sb.length() - 2);
            sb.append(".\n");
        }
        sb.append("-------------------------------------\n");
        return sb.toString();
    }
}
