package com.mindary.aichat.dto.amqp;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiaryAnalysisDto {

    private UUID senderId; // Assuming the analysis service includes its ID
    private UUID diaryId;
    private UUID userId; // Crucial for associating with the user
    private EmotionDto emotion;
    private List<CorrelationDto> correlations;
    private List<SymptomDto> symptoms;
}
