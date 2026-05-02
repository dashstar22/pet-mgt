package com.petmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long breedId;
    private String gender;
    private Integer age;
    private BigDecimal weight;
    private String healthStatus;
    private String vaccineStatus;
    private String sterilizationStatus;
    private String personality;
    private String adoptionRequirement;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String breedName;

    @TableField(exist = false)
    private String coverImageUrl;
}
