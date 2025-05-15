package com.mindary.aichat.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "diaries")
public class DiaryAnalysis {

    @Id
    private String id;
    private String senderId;
    private String diaryId;
    private Emotion emotion;
    private List<Correlation> correlations;
    private List<Symptom> symptoms;
    private String createdAt;
    private String updatedAt;

    @Data
    public static class Emotion {

        private String emotionLevel;
        private List<String> category;
        private String summary;
    }

    @Data
    public static class Correlation {

        private String name;
        private String description;
    }

    @Data
    public static class Symptom {

        private String name;
        private String risk;
        private String description;
        private String suggestions;
    }
}
