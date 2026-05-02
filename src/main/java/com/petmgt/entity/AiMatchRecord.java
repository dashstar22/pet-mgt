package com.petmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_match_record")
public class AiMatchRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String preferenceText;
    private String resultText;
    private Long recommendedPetId;
    private Integer matchScore;
    private LocalDateTime createdAt;
}
