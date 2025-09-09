package com.focusflow.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StudentAgent {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    // Rules cache received from server
    private static final List<Rule> RULES = new CopyOnWriteArrayList<>();
    // Process names we should never kill
    private static final Set<String> EXEMPT = Set.of(
            "system", "idle", "services.exe", "smss.exe", "lsass.exe", "csrss.exe",
            "wininit.exe", "winlogon.exe", "svchost.exe", "fontdrvhost.exe", "registry",
            "explorer.exe", "dwm.exe", "java.exe", "javaw.exe"
    );

    // List of process names to block (e.g., "chrome.exe", "notepad.exe")
    private static final Set<String> BLOCKED = Set.of("chrome.exe", "notepad.exe");

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar student-agent.jar <clientId> <wsUrl>");
            return;
        }

        String clientId = args[0];
        String wsUrl = args[1];

        System.out.println("Starting StudentAgent with id: " + clientId + " connecting to " + wsUrl);

        // Start enforcement loop
        startEnforcer();

        // Connect WS and register
        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<WebSocket> wsFuture = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(URI.create(wsUrl), new Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        System.out.println("Connected. Registering...");
                        webSocket.sendText(jsonRegister(clientId), true);
                        Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        String msg = data.toString();
                        System.out.println("WS Message: " + msg);
                        handleMessage(msg);
                        return Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        System.err.println("WebSocket error: " + error.getMessage());
                        Listener.super.onError(webSocket, error);
                    }
                });

        // Keep JVM alive
        wsFuture.join();
        try {
            // Keep running indefinitely
            Thread.currentThread().join();
        } catch (InterruptedException ignored) { }
    }

    private static String jsonRegister(String clientId) {
        return "{\"type\":\"register\",\"clientId\":\"" + clientId + "\"}";
    }

    private static void handleMessage(String msg) {
        try {
            JsonNode root = MAPPER.readTree(msg);
            String type = root.path("type").asText("");
            if ("rules".equalsIgnoreCase(type)) {
                List<Rule> newRules = new ArrayList<>();
                for (JsonNode n : root.path("rules")) {
                    Rule r = new Rule();
                    r.type = n.path("type").asText("");
                    r.targetType = n.path("targetType").asText("");
                    r.pattern = n.path("pattern").asText("");
                    r.enabled = n.path("enabled").asBoolean(true);
                    newRules.add(r);
                }
                RULES.clear();
                RULES.addAll(newRules);
                System.out.println("Applied rules: " + RULES.size());
            }
        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }

    private static void startEnforcer() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "app-blocker-enforcer");
            t.setDaemon(true);
            return t;
        });
        exec.scheduleAtFixedRate(StudentAgent::enforceBlacklist, 0, 2, TimeUnit.SECONDS);
    }

    private static void enforceBlacklist() {
        // Collect APP blacklist patterns
        Set<String> patterns = new HashSet<>();
        for (Rule r : RULES) {
            if (r.enabled && equalsIgnoreCase(r.targetType, "APP") && equalsIgnoreCase(r.type, "BLACKLIST")) {
                if (r.pattern != null && !r.pattern.isBlank()) {
                    patterns.add(r.pattern.toLowerCase(Locale.ROOT));
                }
            }
        }
        if (patterns.isEmpty()) return;

        ProcessHandle.allProcesses().forEach(ph -> {
            try {
                String cmd = ph.info().command().orElse("");
                if (cmd.isBlank()) return;
                String exe = Paths.get(cmd).getFileName().toString().toLowerCase(Locale.ROOT);
                if (EXEMPT.contains(exe)) return;
                boolean match = patterns.contains(exe) || patterns.stream().anyMatch(exe::contains);
                if (match && ph.isAlive()) {
                    System.out.println("Blocking app (killing process): " + exe + " [pid=" + ph.pid() + "]");
                    ph.destroy();
                    // fallback to force if still alive shortly after
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                    if (ph.isAlive()) ph.destroyForcibly();
                }
            } catch (Throwable ignored) { }
        });
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }

    // Minimal rule shape matching backend payload
    static class Rule {
        String type;        // BLACKLIST | WHITELIST
        String targetType;  // WEBSITE | APP
        String pattern;     // e.g., chrome.exe
        boolean enabled;
    }
}
