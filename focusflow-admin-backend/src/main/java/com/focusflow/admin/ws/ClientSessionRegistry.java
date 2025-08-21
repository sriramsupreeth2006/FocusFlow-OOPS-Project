package com.focusflow.admin.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClientSessionRegistry {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void put(String clientId, WebSocketSession session) {
        sessions.put(clientId, session);
    }

    public WebSocketSession get(String clientId) {
        return sessions.get(clientId);
    }

    public Map<String, WebSocketSession> all() { return sessions; }

    public void remove(String clientId) {
        sessions.remove(clientId);
    }
}
