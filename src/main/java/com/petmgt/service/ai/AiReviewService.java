package com.petmgt.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petmgt.dto.AiReviewResult;
import com.petmgt.entity.AiReviewRecord;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.AiReviewRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(AiReviewService.class);

    private final AiService aiService;
    private final AiReviewRecordMapper reviewRecordMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiReviewService(AiService aiService, AiReviewRecordMapper reviewRecordMapper) {
        this.aiService = aiService;
        this.reviewRecordMapper = reviewRecordMapper;
    }

    public AiReviewResult review(Application application, Pet pet) {
        String systemPrompt = """
            你是一个宠物领养审核辅助助手。分析申请人信息与宠物的匹配程度。
            输出 JSON（不要包含 markdown 代码块标记）:
            {"score":78,"strengths":"优势分析","risks":"风险分析",
             "suggestion":"建议通过/谨慎通过/建议拒绝","notes":"补充说明"}
            只提供参考建议，不代替管理员决策。
            """;

        String userMessage = buildReviewPrompt(application, pet);
        String aiResponse = aiService.chat(systemPrompt, userMessage);

        if (aiResponse == null) {
            log.warn("AI 审核建议生成失败");
            return null;
        }

        AiReviewResult result = parseReviewResult(aiResponse);
        if (result != null) {
            saveReviewRecord(application.getId(), aiResponse, result);
        }

        return result;
    }

    private String buildReviewPrompt(Application app, Pet pet) {
        return String.format("""
            申请人信息：
            - 电话：%s
            - 地址：%s
            - 养宠经验：%s
            - 每日陪伴时间：%s
            - 申请理由：%s

            宠物信息：
            - 名称：%s
            - 品种：%s
            - 性别：%s
            - 年龄：%d岁
            - 健康状况：%s
            - 性格：%s
            - 领养要求：%s
            """,
            app.getPhone(), app.getAddress(),
            app.getExperience() != null ? app.getExperience() : "未填写",
            app.getAccompanyTime(), app.getReason(),
            pet.getName(), pet.getBreedName(), pet.getGender(), pet.getAge(),
            pet.getHealthStatus(), pet.getPersonality(),
            pet.getAdoptionRequirement() != null ? pet.getAdoptionRequirement() : "无特殊要求");
    }

    private AiReviewResult parseReviewResult(String aiResponse) {
        try {
            String json = aiResponse.trim();
            int jsonStart = json.indexOf("{");
            int jsonEnd = json.lastIndexOf("}");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                json = json.substring(jsonStart, jsonEnd + 1);
            }
            return objectMapper.readValue(json, AiReviewResult.class);
        } catch (Exception e) {
            log.error("解析 AI 审核结果失败", e);
            return null;
        }
    }

    private void saveReviewRecord(Long applicationId, String aiResponse, AiReviewResult result) {
        try {
            AiReviewRecord record = new AiReviewRecord();
            record.setApplicationId(applicationId);
            record.setResultText(aiResponse);
            record.setScore(result.getScore());
            record.setSuggestion(result.getSuggestion());
            reviewRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("保存 AI 审核记录失败", e);
        }
    }
}
