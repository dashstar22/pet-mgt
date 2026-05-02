package com.petmgt.dto;

import lombok.Data;

@Data
public class AiMatchResult {
    private Long petId;
    private String petName;
    private Integer matchScore;
    private String reason;
    private String notes;
    private boolean suggestApply;
    private String coverImageUrl;
}
