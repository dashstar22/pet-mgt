package com.petmgt.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.AiMatchRequest;
import com.petmgt.dto.AiMatchResult;
import com.petmgt.entity.AiMatchRecord;
import com.petmgt.service.ai.AiMatchService;
import com.petmgt.util.SecurityUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

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
                        RedirectAttributes redirectAttributes,
                        HttpSession session) {
        try {
            Long userId = SecurityUtil.getCurrentUser().getId();
            Map<String, Object> outcome = aiMatchService.match(request, userId);
            @SuppressWarnings("unchecked")
            List<AiMatchResult> results = (List<AiMatchResult>) outcome.get("results");
            boolean aiUsed = (boolean) outcome.get("aiUsed");

            if (results.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning",
                    "暂无可匹配的宠物，请先添加可领养宠物或调整匹配条件");
                return "redirect:/user/ai-match";
            }

            session.setAttribute("matchResults", results);
            session.setAttribute("matchAiUsed", aiUsed);
            return "redirect:/user/ai-match/result";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("warning",
                "匹配服务暂时不可用，请稍后再试");
            return "redirect:/user/ai-match";
        }
    }

    @GetMapping("/result")
    public String matchResult(Model model, HttpSession session) {
        model.addAttribute("title", "AI 匹配结果");
        @SuppressWarnings("unchecked")
        List<AiMatchResult> results = (List<AiMatchResult>) session.getAttribute("matchResults");
        if (results != null) {
            model.addAttribute("results", results);
            model.addAttribute("aiUsed", session.getAttribute("matchAiUsed"));
        }
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

    @PostMapping("/history/clear")
    public String clearHistory(RedirectAttributes redirectAttributes) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        aiMatchService.deleteUserHistory(userId);
        redirectAttributes.addFlashAttribute("success", "匹配历史已清空");
        return "redirect:/user/ai-match/history";
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        aiMatchService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
