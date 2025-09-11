package com.focusflow.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.focusflow.admin.dto.RuleRequest;
import com.focusflow.admin.model.Rule;
import com.focusflow.admin.service.RuleService;

@RestController
@RequestMapping("/api/admin/rules")
public class AdminRuleController {
    private final RuleService service;

    public AdminRuleController(RuleService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rule> allRules() {
        return service.getAll();
    }

    @PostMapping
    public Rule addRule(@RequestBody RuleRequest req) {
        Rule r = new Rule();
        try {
            r.setType(Rule.RuleType.valueOf(req.type));
        } catch (Exception e) {
            r.setType(Rule.RuleType.BLACKLIST);
        }
        try {
            r.setTargetType(Rule.TargetType.valueOf(req.targetType));
        } catch (Exception e) {
            r.setTargetType(Rule.TargetType.WEBSITE);
        }
        r.setPattern(req.pattern);
        r.setEnabled(true);
        return service.save(r);
    }
}
