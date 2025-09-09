package com.focusflow.admin.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RuleWebSocketHandler implements WebSocketHandler {

    private final ClientSessionRegistry registry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RuleWebSocketHandler(ClientSessionRegistry registry){
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception { }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        JsonNode node = objectMapper.readTree(payload);
        String type = node.path("type").asText();
        if ("register".equals(type)) {
            String clientId = node.path("clientId").asText();
            registry.put(clientId, session);
            session.sendMessage(new TextMessage("{\"type\":\"registered\",\"clientId\":\"" + clientId + "\"}"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception { }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // naive cleanup
        registry.all().entrySet().removeIf(e -> e.getValue().getId().equals(session.getId()));
    }

    @Override
    public boolean supportsPartialMessages() { return false; }
}
