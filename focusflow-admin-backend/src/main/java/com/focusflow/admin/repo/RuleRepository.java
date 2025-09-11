package com.focusflow.admin.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.focusflow.admin.model.Rule;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    // delete all APP rules
    long deleteByTargetType(Rule.TargetType targetType);
    // delete APP rules filtered by type (BLACKLIST/WHITELIST)
    long deleteByTargetTypeAndType(Rule.TargetType targetType, Rule.RuleType type);
}
