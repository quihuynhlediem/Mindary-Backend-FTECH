package com.mindary.aichat.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mindary.aichat.dto.amqp.DiaryAnalysisDto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "user_diary_insights") // New collection name
public class UserDiaryInsight {

    @Id
    private String id; // MongoDB document ID

    @Indexed(unique = true) // Ensure only one document per user
    private UUID userId;

    private DiaryAnalysisDto latestAnalysis; // Embed the DTO received from RabbitMQ

    private LocalDateTime lastUpdatedAt;

    public UserDiaryInsight(UUID userId, DiaryAnalysisDto latestAnalysis) {
        this.userId = userId;
        this.latestAnalysis = latestAnalysis;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
