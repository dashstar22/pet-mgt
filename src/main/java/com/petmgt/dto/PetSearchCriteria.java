package com.petmgt.dto;

import lombok.Data;

@Data
public class PetSearchCriteria {
    private String petType;
    private Long breedId;
    private String gender;
    private String status;
    private String name;
}
