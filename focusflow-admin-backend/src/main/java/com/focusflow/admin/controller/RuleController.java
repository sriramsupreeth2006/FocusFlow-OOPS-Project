package com.focusflow.admin.controller;

import com.focusflow.admin.model.Rule;
import com.focusflow.admin.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public Rule create(@Valid @RequestBody Rule rule){ return service.create(rule); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){ service.delete(id); }

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
}
