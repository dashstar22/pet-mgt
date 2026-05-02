package com.petmgt.service.ai;

import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final AiService aiService;

    public AiChatService(AiService aiService) {
        this.aiService = aiService;
    }

    public String chat(String userQuestion) {
        String systemPrompt = """
            你是一个宠物养护问答助手。仅回答与宠物饲养、护理、领养准备
            相关的通用建议问题。如果用户询问疾病诊断、用药、紧急情况，
            请回复：'建议咨询专业兽医，AI 无法提供医疗建议。'
            用中文回答，语言温暖亲切，回答简洁在 200 字以内。
            """;

        String response = aiService.chat(systemPrompt, userQuestion);
        if (response == null) {
            return "AI API key 未配置，请在 application.properties 中设置 ai.deepseek.api-key 或设置环境变量 DEEPSEEK_API_KEY。";
        }
        return response;
    }
}
