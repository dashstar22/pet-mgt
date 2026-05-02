package com.petmgt.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.AiMatchRequest;
import com.petmgt.dto.AiMatchResult;
import com.petmgt.entity.AiMatchRecord;
import com.petmgt.service.ai.AiMatchService;
import com.petmgt.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user/ai-match")
public class AiMatchController {

    private final AiMatchService aiMatchService;

    public AiMatchController(AiMatchService aiMatchService) {
        this.aiMatchService = aiMatchService;
    }

    @GetMapping
    public String matchForm(Model model) {
        model.addAttribute("title", "AI 宠物匹配");
        model.addAttribute("request", new AiMatchRequest());
        return "user/ai-match";
    }

    @PostMapping
    public String match(@ModelAttribute AiMatchRequest request,
                        RedirectAttributes redirectAttributes) {
        try {
            Long userId = SecurityUtil.getCurrentUser().getId();
            List<AiMatchResult> results = aiMatchService.match(request, userId);

            if (results.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning",
                    "暂无可匹配的宠物，或 AI 服务暂时不可用，请稍后再试");
                return "redirect:/user/ai-match";
            }

            redirectAttributes.addFlashAttribute("results", results);
            return "redirect:/user/ai-match/result";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("warning",
                "匹配服务暂时不可用，请稍后再试");
            return "redirect:/user/ai-match";
        }
    }

    @GetMapping("/result")
    public String matchResult(Model model) {
        model.addAttribute("title", "AI 匹配结果");
        return "user/ai-match-result";
    }

    @GetMapping("/history")
    public String history(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Page<AiMatchRecord> recordPage = aiMatchService.getUserHistory(userId, page, size);
        model.addAttribute("title", "匹配历史");
        model.addAttribute("page", recordPage);
        return "user/ai-match-history";
    }
}
