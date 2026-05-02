package com.petmgt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiReviewResult {
    private int score;
    private String strengths;
    private String risks;
    private String suggestion;
    private String notes;
}
