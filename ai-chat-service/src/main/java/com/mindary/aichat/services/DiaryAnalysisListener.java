package com.mindary.aichat.services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.mindary.aichat.dto.amqp.DiaryAnalysisDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryAnalysisListener {

    private final UserDiaryInsightService userDiaryInsightService;

    @RabbitListener(queues = "${rabbitmq.queue.analysis_result.name}")
    public void receiveDiaryAnalysis(@Payload DiaryAnalysisDto analysisResult) {
        if (analysisResult == null || analysisResult.getUserId() == null) {
            log.warn("Received null analysis result or result without userId. Skipping.");
            return;
        }
        log.info("Received diary analysis result for user: {}", analysisResult.getUserId());

        if (analysisResult.getEmotion() != null) {
            log.info("Emotion: {} ({})", analysisResult.getEmotion().getName(), analysisResult.getEmotion().getScore());
        }
        if (analysisResult.getCorrelations() != null && !analysisResult.getCorrelations().isEmpty()) {
            log.info("Correlations found: {}", analysisResult.getCorrelations().size());
        }
        if (analysisResult.getSymptoms() != null && !analysisResult.getSymptoms().isEmpty()) {
            log.info("Symptoms found: {}", analysisResult.getSymptoms().size());
        }

        userDiaryInsightService.storeLatestInsight(analysisResult.getUserId(), analysisResult);
        log.info("Stored/Updated latest diary insight for user: {}", analysisResult.getUserId());
    }
}
