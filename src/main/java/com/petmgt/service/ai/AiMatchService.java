package com.petmgt.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petmgt.dto.AiMatchRequest;
import com.petmgt.dto.AiMatchResult;
import com.petmgt.entity.AiMatchRecord;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.AiMatchRecordMapper;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiMatchService {

    private static final Logger log = LoggerFactory.getLogger(AiMatchService.class);

    private final AiService aiService;
    private final PetService petService;
    private final AiMatchRecordMapper matchRecordMapper;
    private final BreedMapper breedMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiMatchService(AiService aiService, PetService petService,
                          AiMatchRecordMapper matchRecordMapper,
                          BreedMapper breedMapper) {
        this.aiService = aiService;
        this.petService = petService;
        this.matchRecordMapper = matchRecordMapper;
        this.breedMapper = breedMapper;
    }

    public Map<String, Object> match(AiMatchRequest request, Long userId) {
        List<Pet> availablePets = petService.findAvailable();

        if (availablePets.isEmpty()) {
            log.warn("没有可领养的宠物，匹配终止");
            return Map.of("results", List.of(), "aiUsed", false);
        }

        List<AiMatchResult> results = null;
        boolean aiUsed = false;

        String aiResponse = aiService.chat(buildSystemPrompt(), buildMatchPrompt(request, availablePets));
        if (aiResponse != null) {
            results = parseMatchResult(aiResponse, availablePets);
            if (!results.isEmpty()) {
                aiUsed = true;
            }
        }

        if (!aiUsed) {
            log.info("AI 匹配未返回有效结果，使用规则引擎兜底匹配");
            results = fallbackMatch(request, availablePets);
        }

        saveMatchRecord(request, aiResponse, results, userId, aiUsed);

        Map<String, Object> outcome = new HashMap<>();
        outcome.put("results", results);
        outcome.put("aiUsed", aiUsed);
        return outcome;
    }

    private String buildMatchPrompt(AiMatchRequest request, List<Pet> pets) {
        Map<Long, String> breedTypeMap = breedMapper.selectList(null).stream()
            .collect(Collectors.toMap(Breed::getId, Breed::getPetType, (a, b) -> a));

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
            String petType = breedTypeMap.getOrDefault(pet.getBreedId(), "未知");
            sb.append(String.format("ID=%d, 名称=%s, 类型=%s, 品种=%s, 性别=%s, 年龄=%d岁, 健康状况=%s, 疫苗=%s, 绝育=%s, 性格=%s\n",
                pet.getId(), pet.getName(), petType, pet.getBreedName(), pet.getGender(), pet.getAge(),
                pet.getHealthStatus(), pet.getVaccineStatus() != null ? pet.getVaccineStatus() : "未知",
                pet.getSterilizationStatus() != null ? pet.getSterilizationStatus() : "未知",
                pet.getPersonality()));
        }
        return sb.toString();
    }

    private List<AiMatchResult> parseMatchResult(String aiResponse, List<Pet> availablePets) {
        try {
            // 日志：打印 AI 原始返回内容的前 500 字符
            log.info("AI 原始返回内容(前500字): {}",
                aiResponse.length() > 500 ? aiResponse.substring(0, 500) + "..." : aiResponse);

            String json = aiResponse.trim();
            int jsonStart = json.indexOf("[");
            int jsonEnd = json.lastIndexOf("]");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                json = json.substring(jsonStart, jsonEnd + 1);
            }
            log.info("提取的 JSON: {}", json.length() > 300 ? json.substring(0, 300) + "..." : json);

            List<AiMatchResult> results = objectMapper.readValue(json,
                new TypeReference<List<AiMatchResult>>() {});
            log.info("成功解析出 {} 条匹配结果", results.size());

            Map<Long, String> coverMap = availablePets.stream()
                .filter(p -> p.getCoverImageUrl() != null)
                .collect(Collectors.toMap(Pet::getId, Pet::getCoverImageUrl, (a, b) -> a));

            for (AiMatchResult r : results) {
                r.setCoverImageUrl(coverMap.get(r.getPetId()));
                if (r.getMatchScore() != null) {
                    r.setSuggestApply(r.getMatchScore() >= 60);
                }
            }

            return results;
        } catch (Exception e) {
            log.error("解析 AI 匹配结果失败: {}", e.getMessage());
            return List.of();
        }
    }

    private String buildSystemPrompt() {
        return """
            你是一个宠物领养匹配助手。根据用户偏好和可领养宠物列表，
            为每只宠物打分并推荐最匹配的。返回 JSON 数组格式（只返回 JSON，不要包含 markdown 代码块标记）:
            [{"petId":1,"petName":"Mimi","matchScore":92,
              "reason":"推荐理由","notes":"注意事项","suggestApply":true}]
            打分规则：类型完全匹配+30分，性格匹配+25分，健康可接受+20分，陪伴时间匹配+15分，环境合适+10分（满分100）。
            返回匹配度最高的 3 个结果（如果宠物不足 3 只则全部返回），按分数降序排列。
            即使用户偏好是"不限"，也要根据宠物特点给出合理的分数和推荐。
            低分（40分以下）也要返回，只需将 suggestApply 设为 false。
            """;
    }

    private List<AiMatchResult> fallbackMatch(AiMatchRequest request, List<Pet> pets) {
        Map<Long, String> breedTypeMap = breedMapper.selectList(null).stream()
            .collect(Collectors.toMap(Breed::getId, Breed::getPetType, (a, b) -> a));

        List<AiMatchResult> results = new ArrayList<>();

        for (Pet pet : pets) {
            int score = 0;
            StringBuilder reason = new StringBuilder();
            String petType = breedTypeMap.getOrDefault(pet.getBreedId(), "");
            String personality = pet.getPersonality() != null ? pet.getPersonality() : "";

            // 类型匹配 (30分)
            if (isNotEmpty(request.getPetType()) && request.getPetType().equals(petType)) {
                score += 30;
                reason.append("类型匹配；");
            } else if (!isNotEmpty(request.getPetType())) {
                score += 15;
            }

            // 性格匹配 (25分)
            if (isNotEmpty(request.getPersonality()) && request.getPersonality().equals(personality)) {
                score += 25;
                reason.append("性格匹配；");
            } else if (isNotEmpty(request.getPersonality()) && isPersonalityCompatible(request.getPersonality(), personality)) {
                score += 15;
                reason.append("性格兼容；");
            } else if (!isNotEmpty(request.getPersonality())) {
                score += 12;
            }

            // 健康接受度 (20分)
            String healthAcceptance = request.getHealthAcceptance();
            String healthStatus = pet.getHealthStatus() != null ? pet.getHealthStatus() : "";
            if ("可接受任何状态".equals(healthAcceptance)) {
                score += 20;
                reason.append("健康状况可接受；");
            } else if ("可接受轻微健康问题".equals(healthAcceptance) &&
                       (healthStatus.contains("健康") || healthStatus.contains("轻微"))) {
                score += 20;
                reason.append("健康状况可接受；");
            } else if ("仅健康".equals(healthAcceptance) && "健康".equals(healthStatus)) {
                score += 20;
                reason.append("健康状况理想；");
            } else if (!isNotEmpty(healthAcceptance)) {
                score += 10;
            }

            // 陪伴时间 (15分) - 根据宠物性格推断需要的陪伴时间
            int accompanyScore = matchAccompanyTime(request.getAccompanyTime(), personality);
            score += accompanyScore;
            if (accompanyScore >= 12) reason.append("陪伴时间匹配；");

            // 居住环境 (10分)
            int spaceScore = matchLivingSpace(request.getLivingSpace(), petType);
            score += spaceScore;
            if (spaceScore >= 8) reason.append("居住环境合适；");

            if (score >= 40) {
                AiMatchResult r = new AiMatchResult();
                r.setPetId(pet.getId());
                r.setPetName(pet.getName());
                r.setMatchScore(score);
                r.setReason(reason.toString());
                r.setSuggestApply(score >= 60);
                r.setCoverImageUrl(pet.getCoverImageUrl());
                results.add(r);
            }
        }

        results.sort((a, b) -> b.getMatchScore() - a.getMatchScore());
        if (results.size() > 3) {
            return results.subList(0, 3);
        }
        return results;
    }

    private boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private boolean isPersonalityCompatible(String wanted, String actual) {
        // 温顺亲人 兼容 独立安静、聪明机警
        // 活泼好动 兼容 聪明机警
        // 独立安静 兼容 温顺亲人、聪明机警
        if (wanted.equals(actual)) return true;
        if ("温顺亲人".equals(wanted) && ("独立安静".equals(actual) || "聪明机警".equals(actual))) return true;
        if ("活泼好动".equals(wanted) && "聪明机警".equals(actual)) return true;
        if ("独立安静".equals(wanted) && ("温顺亲人".equals(actual) || "聪明机警".equals(actual))) return true;
        if ("聪明机警".equals(wanted) && ("温顺亲人".equals(actual) || "活泼好动".equals(actual))) return true;
        return false;
    }

    private int matchAccompanyTime(String accompanyTime, String personality) {
        if (!isNotEmpty(accompanyTime)) return 10;
        boolean highNeed = "活泼好动".equals(personality);
        return switch (accompanyTime) {
            case "6小时以上" -> highNeed ? 15 : 12;
            case "3-6小时" -> highNeed ? 12 : 15;
            case "1-3小时" -> highNeed ? 5 : 10;
            case "少于1小时" -> highNeed ? 3 : 5;
            default -> 8;
        };
    }

    private int matchLivingSpace(String livingSpace, String petType) {
        if (!isNotEmpty(livingSpace)) return 5;
        if ("带院子".equals(livingSpace)) return 10;
        if ("普通住宅".equals(livingSpace)) {
            return "狗".equals(petType) ? 8 : 10;
        }
        if ("公寓".equals(livingSpace)) {
            return "狗".equals(petType) ? 4 : ("猫".equals(petType) ? 10 : 8);
        }
        return 5;
    }

    private void saveMatchRecord(AiMatchRequest request, String aiResponse,
                                  List<AiMatchResult> results, Long userId, boolean aiUsed) {
        try {
            AiMatchRecord record = new AiMatchRecord();
            record.setUserId(userId);
            record.setPreferenceText(formatPreference(request));
            record.setResultText(aiResponse != null ? aiResponse : "规则引擎兜底匹配");
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
