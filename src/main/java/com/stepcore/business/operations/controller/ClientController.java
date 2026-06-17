package com.stepcore.business.operations.controller;

import com.stepcore.business.operations.controller.dto.ClientResponse;
import com.stepcore.business.operations.controller.dto.CreateClientRequest;
import com.stepcore.business.operations.controller.dto.UpdateClientRequest;
import com.stepcore.business.operations.service.ClientService;
import com.stepcore.business.security.AppPermissions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operations/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_CLIENTS + "')")
    public List<ClientResponse> findAll(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return clientService.findAll(activeOnly);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_CLIENTS + "')")
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_CLIENTS + "')")
    public ClientResponse findById(@PathVariable Long id) {
        return clientService.findById(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_CLIENTS + "')")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody UpdateClientRequest request) {
        return clientService.update(id, request);
    }
}
