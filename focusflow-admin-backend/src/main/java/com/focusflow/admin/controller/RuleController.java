package com.focusflow.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.focusflow.admin.model.Rule;
import com.focusflow.admin.service.RuleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService service;

    public RuleController(RuleService service){
        this.service = service;
    }

    @GetMapping
    public List<Rule> list(){ return service.list(); }

    @PostMapping
    public Rule create(@Valid @RequestBody Rule rule){
        Rule saved = service.create(rule);
        service.exportAppBlacklistToFile();
        return saved;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        service.delete(id);
        service.exportAppBlacklistToFile();
    }

    @PostMapping("/push")
    public Map<String,Object> pushAll(){
        int pushed = service.pushToAll();
        return Map.of("pushedSessions", pushed);
    }

    @PostMapping("/push/{clientId}")
    public Map<String,Object> pushClient(@PathVariable String clientId){
        int pushed = service.pushToClient(clientId);
        return Map.of("pushedSessions", pushed, "clientId", clientId);
    }

    // Seed a few APP blacklist rules and push to agents
    @PostMapping("/seed/apps")
    public Map<String,Object> seedApps() {
        List<String> apps = List.of("chrome.exe", "discord.exe", "vlc.exe");
        for (String p : apps) {
            Rule r = new Rule();
            r.setType(Rule.RuleType.BLACKLIST);
            r.setTargetType(Rule.TargetType.APP);
            r.setPattern(p);
            r.setEnabled(true);
            service.save(r);
        }
        int pushed = service.pushToAll();
        return Map.of("created", apps.size(), "pushedSessions", pushed);
    }

    // Delete ALL APP rules (BLACKLIST + WHITELIST). Optional push to agents.
    @DeleteMapping("/apps")
    public Map<String,Object> deleteAllAppRules(@RequestParam(defaultValue = "false") boolean push) {
        long deleted = service.deleteAllAppRules();
        int pushed = push ? service.pushToAll() : 0;
        return Map.of("deleted", deleted, "pushedSessions", pushed);
    }

    // Delete APP rules by type (BLACKLIST or WHITELIST). Optional push.
    @DeleteMapping("/apps/{type}")
    public Map<String,Object> deleteAppRulesByType(@PathVariable String type,
                                                   @RequestParam(defaultValue = "false") boolean push) {
        Rule.RuleType t;
        try { t = Rule.RuleType.valueOf(type.toUpperCase()); }
        catch (Exception e) { t = Rule.RuleType.BLACKLIST; }
        long deleted = service.deleteAppRulesByType(t);
        int pushed = push ? service.pushToAll() : 0;
        return Map.of("deleted", deleted, "type", t.name(), "pushedSessions", pushed);
    }
}
