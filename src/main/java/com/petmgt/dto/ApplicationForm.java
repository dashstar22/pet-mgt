package com.petmgt.dto;

import lombok.Data;

@Data
public class ApplicationForm {
    private Long petId;
    private String phone;
    private String address;
    private String experience;
    private String accompanyTime;
    private String reason;
}
