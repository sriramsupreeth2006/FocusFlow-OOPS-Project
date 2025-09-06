package com.focusflow.admin.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.focusflow.admin.model.Rule;
import com.focusflow.admin.repo.RuleRepository;
import com.focusflow.admin.ws.ClientSessionRegistry;

@Service
public class RuleService {
    private final RuleRepository repo;
    private final ClientSessionRegistry sessions;
    public RuleService(RuleRepository repo, ClientSessionRegistry sessions) {
        this.repo = repo;
        this.sessions = sessions;
    }

    public List<Rule> list() { return repo.findAll(); }

    public Rule create(Rule r) { return repo.save(r); }

    // compatibility aliases used by other controllers
    public List<Rule> getAll() { return list(); }
    public Rule save(Rule r) { return create(r); }

    public void delete(Long id) { repo.deleteById(id); }

    @Transactional
    public long deleteAllAppRules() {
        return repo.deleteByTargetType(Rule.TargetType.APP);
    }

    @Transactional
    public long deleteAppRulesByType(Rule.RuleType type) {
        return repo.deleteByTargetTypeAndType(Rule.TargetType.APP, type);
    }

    public int pushToAll() {
        List<Rule> rules = repo.findAll();
        String payload = "{\"type\":\"rules\",\"rules\":" + JsonUtil.toJson(rules) + "}";
        AtomicInteger sent = new AtomicInteger();
        sessions.all().forEach((id, session) -> {
            try {
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                    sent.incrementAndGet();
                }
            } catch (Exception ignored) {}
        });
        return sent.get();
    }

    public int pushToClient(String clientId) {
        WebSocketSession session = sessions.get(clientId);
        if (session == null || !session.isOpen()) return 0;
        String payload = "{\"type\":\"rules\",\"rules\":" + JsonUtil.toJson(repo.findAll()) + "}";
        try {
            session.sendMessage(new TextMessage(payload));
            return 1;
        } catch (Exception e) { return 0; }
    }

    public void exportAppBlacklistToFile() {
        List<Rule> rules = repo.findAll();
        Path file = Path.of("..", "..", "block_apps", "blocklist.txt"); // relative to backend folder
        StringBuilder sb = new StringBuilder();
        for (Rule r : rules) {
            if (r.getTargetType() == Rule.TargetType.APP && r.getType() == Rule.RuleType.BLACKLIST && r.isEnabled()) {
                sb.append(r.getPattern()).append(System.lineSeparator());
            }
        }
        try {
            Files.writeString(file, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
