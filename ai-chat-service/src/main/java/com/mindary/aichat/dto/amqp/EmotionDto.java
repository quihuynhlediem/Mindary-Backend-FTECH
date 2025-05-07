package com.mindary.aichat.dto.amqp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmotionDto {

    private String name;
    private double score;
    private String description;
}
