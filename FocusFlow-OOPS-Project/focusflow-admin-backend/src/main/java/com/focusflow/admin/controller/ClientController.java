package com.focusflow.admin.controller;

import com.focusflow.admin.dto.ClientRegisterRequest;
import com.focusflow.admin.model.Client;
import com.focusflow.admin.service.ClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public Client register(@RequestBody ClientRegisterRequest req) {
        return service.register(req.hostname, req.ip);
    }

    @GetMapping
    public List<Client> allClients() {
        return service.getAll();
    }
}
