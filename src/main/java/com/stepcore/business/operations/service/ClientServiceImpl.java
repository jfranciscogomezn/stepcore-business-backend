package com.stepcore.business.operations.service;

import com.stepcore.business.exception.ClientNotFoundException;
import com.stepcore.business.exception.DuplicateClientNameException;
import com.stepcore.business.operations.controller.dto.ClientResponse;
import com.stepcore.business.operations.controller.dto.CreateClientRequest;
import com.stepcore.business.operations.controller.dto.UpdateClientRequest;
import com.stepcore.business.operations.domain.model.Client;
import com.stepcore.business.operations.domain.model.ClientStatus;
import com.stepcore.business.operations.repository.ClientRepository;
import com.stepcore.business.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public ClientResponse create(final CreateClientRequest request) {
        final Long tenantId = TenantContext.getTenantIdOrDefault();
        clientRepository.findByTenantIdAndNameIgnoreCase(tenantId, request.name())
                .ifPresent(c -> { throw new DuplicateClientNameException(request.name()); });

        final Client client = clientRepository.save(Client.builder()
                .withName(request.name())
                .withTaxId(request.taxId())
                .withContactName(request.contactName())
                .withContactEmail(request.contactEmail())
                .withContactPhone(request.contactPhone())
                .withInternalNotes(request.internalNotes())
                .withStatus(ClientStatus.ACTIVE)
                .build());
        return toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll(final boolean activeOnly) {
        final Long tenantId = TenantContext.getTenantIdOrDefault();
        final List<Client> clients = activeOnly
                ? clientRepository.findAllByTenantIdAndStatusOrderByNameAsc(tenantId, ClientStatus.ACTIVE)
                : clientRepository.findAllByTenantIdOrderByNameAsc(tenantId);
        return clients.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse findById(final Long id) {
        return toResponse(fetchOrThrow(id));
    }

    @Override
    @Transactional
    public ClientResponse update(final Long id, final UpdateClientRequest request) {
        final Client client = fetchOrThrow(id);
        final Long tenantId = TenantContext.getTenantIdOrDefault();

        if (request.name() != null && !request.name().equalsIgnoreCase(client.getName())) {
            clientRepository.findByTenantIdAndNameIgnoreCase(tenantId, request.name())
                    .ifPresent(c -> { throw new DuplicateClientNameException(request.name()); });
            client.setName(request.name());
        }
        if (request.taxId() != null)       client.setTaxId(request.taxId());
        if (request.contactName() != null)  client.setContactName(request.contactName());
        if (request.contactEmail() != null) client.setContactEmail(request.contactEmail());
        if (request.contactPhone() != null) client.setContactPhone(request.contactPhone());
        if (request.internalNotes() != null) client.setInternalNotes(request.internalNotes());
        if (request.status() != null) {
            client.setStatus(ClientStatus.valueOf(request.status()));
        }
        return toResponse(clientRepository.save(client));
    }

    private Client fetchOrThrow(final Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    private ClientResponse toResponse(final Client c) {
        return new ClientResponse(
                c.getId(), c.getName(), c.getTaxId(),
                c.getContactName(), c.getContactEmail(), c.getContactPhone(),
                c.getInternalNotes(), c.getStatus().name(), c.getCreatedAt());
    }
}
