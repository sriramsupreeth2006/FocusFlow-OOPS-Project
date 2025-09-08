package com.focusflow.admin.service;

import com.focusflow.admin.model.Client;
import com.focusflow.admin.repo.ClientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClientService {
    private final ClientRepository repo;

    public ClientService(ClientRepository repo) {
        this.repo = repo;
    }

    public Client register(String hostname, String ip) {
        Client c = new Client();
        c.setHostname(hostname);
        c.setIp(ip);
        c.setLastSeen(LocalDateTime.now());
        return repo.save(c);
    }

    public List<Client> getAll() {
        return repo.findAll();
    }
}
