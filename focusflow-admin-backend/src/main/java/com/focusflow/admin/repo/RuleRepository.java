package com.focusflow.admin.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.focusflow.admin.model.Rule;

public interface RuleRepository extends JpaRepository<Rule, Long> { }
