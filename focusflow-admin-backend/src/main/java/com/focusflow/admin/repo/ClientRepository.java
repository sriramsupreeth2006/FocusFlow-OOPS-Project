package com.focusflow.admin.repo;

import com.focusflow.admin.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> { }

