package com.focusflow.admin.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RuleWebSocketHandler ruleHandler;

    public WebSocketConfig(RuleWebSocketHandler ruleHandler) {
        this.ruleHandler = ruleHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ruleHandler, "/ws").setAllowedOriginPatterns("*");
    }
}
