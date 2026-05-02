package com.petmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_review_record")
public class AiReviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private String resultText;
    private Integer score;
    private String suggestion;
    private LocalDateTime createdAt;
}
