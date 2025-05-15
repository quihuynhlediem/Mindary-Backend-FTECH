package com.mindary.aichat.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mindary.aichat.models.DiaryAnalysis;
import com.mindary.aichat.repositories.DiaryAnalysisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryAnalysisService {

    private final DiaryAnalysisRepository diaryAnalysisRepository;

    public List<DiaryAnalysis> getLatestAnalyses(String userId, int limit) {
        return diaryAnalysisRepository.findLatestBySenderId(userId, limit);
    }

    public String getLatestAnalysisSummary(String userId) {
        List<DiaryAnalysis> analyses = getLatestAnalyses(userId, 1);
        if (analyses.isEmpty()) {
            return null;
        }

        DiaryAnalysis latest = analyses.get(0);
        StringBuilder summary = new StringBuilder();

        // Add emotion summary
        if (latest.getEmotion() != null) {
            summary.append("Emotional State: ").append(latest.getEmotion().getSummary()).append("\n");
        }

        // Add correlations
        if (latest.getCorrelations() != null && !latest.getCorrelations().isEmpty()) {
            summary.append("\nKey Patterns:\n");
            latest.getCorrelations().forEach(correlation
                    -> summary.append("- ").append(correlation.getDescription()).append("\n")
            );
        }

        // Add symptoms
        if (latest.getSymptoms() != null && !latest.getSymptoms().isEmpty()) {
            summary.append("\nNotable Symptoms:\n");
            latest.getSymptoms().forEach(symptom
                    -> summary.append("- ").append(symptom.getDescription())
                            .append(" (Risk: ").append(symptom.getRisk()).append(")\n")
            );
        }

        return summary.toString();
    }
}
