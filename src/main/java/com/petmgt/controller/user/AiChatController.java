package com.petmgt.controller.user;

import com.petmgt.service.ai.AiChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/user/ai-chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping
    public String chatPage(Model model) {
        model.addAttribute("title", "养宠问答");
        return "user/ai-chat";
    }

    @PostMapping
    @ResponseBody
    public Map<String, String> ask(@RequestParam String question) {
        String answer = aiChatService.chat(question);
        return Map.of("answer", answer);
    }
}
