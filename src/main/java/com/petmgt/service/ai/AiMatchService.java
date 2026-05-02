package com.petmgt.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petmgt.dto.AiMatchRequest;
import com.petmgt.dto.AiMatchResult;
import com.petmgt.entity.AiMatchRecord;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.AiMatchRecordMapper;
import com.petmgt.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiMatchService {

    private static final Logger log = LoggerFactory.getLogger(AiMatchService.class);

    private final AiService aiService;
    private final PetService petService;
    private final AiMatchRecordMapper matchRecordMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiMatchService(AiService aiService, PetService petService,
                          AiMatchRecordMapper matchRecordMapper) {
        this.aiService = aiService;
        this.petService = petService;
        this.matchRecordMapper = matchRecordMapper;
    }

    public List<AiMatchResult> match(AiMatchRequest request, Long userId) {
        List<Pet> availablePets = petService.findAvailable();

        if (availablePets.isEmpty()) {
            return List.of();
        }

        String systemPrompt = """
            你是一个宠物领养匹配助手。根据用户偏好和可领养宠物列表，
            分析并推荐最匹配的宠物。返回 JSON 数组格式（不要包含 markdown 代码块标记）:
            [{"petId":1,"petName":"Mimi","matchScore":92,
              "reason":"推荐理由","notes":"注意事项","suggestApply":true}]
            只推荐 matchScore >= 60 的，按分数降序排列，最多 5 个。
            匹配维度优先级：宠物类型 > 性格兼容 > 健康状态接受度 > 陪伴时间 > 居住环境。
            """;

        String userMessage = buildMatchPrompt(request, availablePets);
        String aiResponse = aiService.chat(systemPrompt, userMessage);

        if (aiResponse == null) {
            log.warn("AI API 返回 null，匹配失败");
            return List.of();
        }

        List<AiMatchResult> results = parseMatchResult(aiResponse, availablePets);
        saveMatchRecord(request, aiResponse, results, userId);

        return results;
    }

    private String buildMatchPrompt(AiMatchRequest request, List<Pet> pets) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户偏好：\n");
        sb.append("- 期望宠物类型：").append(request.getPetType() != null ? request.getPetType() : "不限").append("\n");
        sb.append("- 期望性格：").append(request.getPersonality() != null ? request.getPersonality() : "不限").append("\n");
        sb.append("- 可接受健康状态：").append(request.getHealthAcceptance() != null ? request.getHealthAcceptance() : "不限").append("\n");
        sb.append("- 可陪伴时间：").append(request.getAccompanyTime() != null ? request.getAccompanyTime() : "不限").append("\n");
        sb.append("- 居住环境：").append(request.getLivingSpace() != null ? request.getLivingSpace() : "不限").append("\n");
        sb.append("- 养宠经验：").append(request.getExperience() != null ? request.getExperience() : "不限").append("\n\n");

        sb.append("可领养宠物列表：\n");
        for (Pet pet : pets) {
            sb.append(String.format("ID=%d, 名称=%s, 品种=%s, 类型由品种推断, 性别=%s, 年龄=%d岁, 健康状况=%s, 疫苗=%s, 绝育=%s, 性格=%s\n",
                pet.getId(), pet.getName(), pet.getBreedName(), pet.getGender(), pet.getAge(),
                pet.getHealthStatus(), pet.getVaccineStatus() != null ? pet.getVaccineStatus() : "未知",
                pet.getSterilizationStatus() != null ? pet.getSterilizationStatus() : "未知",
                pet.getPersonality()));
        }
        return sb.toString();
    }

    private List<AiMatchResult> parseMatchResult(String aiResponse, List<Pet> availablePets) {
        try {
            String json = aiResponse.trim();
            int jsonStart = json.indexOf("[");
            int jsonEnd = json.lastIndexOf("]");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                json = json.substring(jsonStart, jsonEnd + 1);
            }

            List<AiMatchResult> results = objectMapper.readValue(json,
                new TypeReference<List<AiMatchResult>>() {});

            Map<Long, String> coverMap = availablePets.stream()
                .filter(p -> p.getCoverImageUrl() != null)
                .collect(Collectors.toMap(Pet::getId, Pet::getCoverImageUrl, (a, b) -> a));

            for (AiMatchResult r : results) {
                r.setCoverImageUrl(coverMap.get(r.getPetId()));
            }

            return results;
        } catch (Exception e) {
            log.error("解析 AI 匹配结果失败", e);
            return List.of();
        }
    }

    private void saveMatchRecord(AiMatchRequest request, String aiResponse,
                                  List<AiMatchResult> results, Long userId) {
        try {
            AiMatchRecord record = new AiMatchRecord();
            record.setUserId(userId);
            record.setPreferenceText(formatPreference(request));
            record.setResultText(aiResponse);
            if (!results.isEmpty()) {
                record.setRecommendedPetId(results.get(0).getPetId());
                record.setMatchScore(results.get(0).getMatchScore());
            }
            matchRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("保存匹配记录失败", e);
        }
    }

    private String formatPreference(AiMatchRequest request) {
        return String.format("类型=%s, 性格=%s, 健康=%s, 时间=%s, 环境=%s, 经验=%s",
            request.getPetType(), request.getPersonality(), request.getHealthAcceptance(),
            request.getAccompanyTime(), request.getLivingSpace(), request.getExperience());
    }

    public void deleteUserHistory(Long userId) {
        LambdaQueryWrapper<AiMatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMatchRecord::getUserId, userId);
        matchRecordMapper.delete(wrapper);
    }

    public void deleteById(Long id) {
        matchRecordMapper.deleteById(id);
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<AiMatchRecord> getUserHistory(Long userId, int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AiMatchRecord> p =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiMatchRecord> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(AiMatchRecord::getUserId, userId)
               .orderByDesc(AiMatchRecord::getCreatedAt);
        return matchRecordMapper.selectPage(p, wrapper);
    }
}
