package com.petmgt.dto;

import lombok.Data;

@Data
public class AiMatchRequest {
    private String petType;
    private String personality;
    private String healthAcceptance;
    private String accompanyTime;
    private String livingSpace;
    private String experience;
}
