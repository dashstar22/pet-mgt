package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.AiMatchRecord;
import com.petmgt.entity.User;
import com.petmgt.mapper.AiMatchRecordMapper;
import com.petmgt.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/ai-records")
public class AiRecordController {

    private final AiMatchRecordMapper aiMatchRecordMapper;
    private final UserMapper userMapper;

    public AiRecordController(AiMatchRecordMapper aiMatchRecordMapper, UserMapper userMapper) {
        this.aiMatchRecordMapper = aiMatchRecordMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<AiMatchRecord> p = new Page<>(page, size);
        LambdaQueryWrapper<AiMatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AiMatchRecord::getCreatedAt);
        Page<AiMatchRecord> result = aiMatchRecordMapper.selectPage(p, wrapper);

        List<Long> userIds = result.getRecords().stream()
            .map(AiMatchRecord::getUserId).distinct().collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, String> usernameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
            for (AiMatchRecord r : result.getRecords()) {
                r.setUsername(usernameMap.get(r.getUserId()));
            }
        }

        model.addAttribute("title", "AI 匹配记录");
        model.addAttribute("page", result);
        return "admin/ai-records";
    }

    @PostMapping("/clear/{userId}")
    public String clearUserHistory(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        LambdaQueryWrapper<AiMatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMatchRecord::getUserId, userId);
        aiMatchRecordMapper.delete(wrapper);
        redirectAttributes.addFlashAttribute("success", "已清空该用户的匹配历史");
        return "redirect:/admin/ai-records";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        aiMatchRecordMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
