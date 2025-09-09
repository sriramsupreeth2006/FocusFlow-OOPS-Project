package com.focusflow.admin.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;

@Entity
public class Rule {
    public enum RuleType { WHITELIST, BLACKLIST }
    public enum TargetType { WEBSITE, APP }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RuleType type;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TargetType targetType;

    @Column(nullable = false)
    private String pattern; // e.g., "youtube.com", "chrome.exe"

    private boolean enabled = true;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    // getters/setters …
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RuleType getType() { return type; }
    public void setType(RuleType type) { this.type = type; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate public void touch() { updatedAt = Instant.now(); }
}
