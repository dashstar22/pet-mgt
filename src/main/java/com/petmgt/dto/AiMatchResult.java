package com.petmgt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiMatchResult {
    private Long petId;
    private String petName;
    private Integer matchScore;
    private String reason;
    private String notes;
    private boolean suggestApply;
    private String coverImageUrl;
}
