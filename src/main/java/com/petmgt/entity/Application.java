package com.petmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("adoption_application")
public class Application {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private Long userId;
    private String phone;
    private String address;
    private String experience;
    private String accompanyTime;
    private String reason;
    private String status;
    private String reviewComment;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String petName;

    @TableField(exist = false)
    private String breedName;

    @TableField(exist = false)
    private String applicantUsername;

    @TableField(exist = false)
    private String coverImageUrl;
}
