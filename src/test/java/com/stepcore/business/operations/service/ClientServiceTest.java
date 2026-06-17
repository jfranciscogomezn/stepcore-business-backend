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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private ClientRepository clientRepository;
    private ClientServiceImpl service;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(2L);
        service = new ClientServiceImpl(clientRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_success() {
        final CreateClientRequest req = new CreateClientRequest(
                "Acme Corp", "123456789", "John", "john@acme.com", "555-0100", "VIP");
        when(clientRepository.findByTenantIdAndNameIgnoreCase(2L, "Acme Corp"))
                .thenReturn(Optional.empty());
        final Client saved = clientWithId(1L, "Acme Corp");
        when(clientRepository.save(any())).thenReturn(saved);

        final ClientResponse result = service.create(req);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Acme Corp");
    }

    @Test
    void create_duplicateName_throwsConflict() {
        when(clientRepository.findByTenantIdAndNameIgnoreCase(2L, "Acme Corp"))
                .thenReturn(Optional.of(clientWithId(1L, "Acme Corp")));
        final CreateClientRequest req = new CreateClientRequest(
                "Acme Corp", null, null, null, null, null);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(DuplicateClientNameException.class);
    }

    @Test
    void findAll_returnsAllWhenNotActiveOnly() {
        when(clientRepository.findAllByTenantIdOrderByNameAsc(2L))
                .thenReturn(List.of(clientWithId(1L, "A"), clientWithId(2L, "B")));

        final List<ClientResponse> result = service.findAll(false);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAll_activeOnly_filtersStatus() {
        when(clientRepository.findAllByTenantIdAndStatusOrderByNameAsc(2L, ClientStatus.ACTIVE))
                .thenReturn(List.of(clientWithId(1L, "Active Corp")));

        final List<ClientResponse> result = service.findAll(true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
    }

    @Test
    void findById_notFound_throws404() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void update_changesFields() {
        final Client existing = clientWithId(5L, "Old Name");
        when(clientRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(clientRepository.findByTenantIdAndNameIgnoreCase(2L, "New Name"))
                .thenReturn(Optional.empty());
        when(clientRepository.save(any())).thenReturn(existing);
        final UpdateClientRequest req = new UpdateClientRequest(
                "New Name", null, null, null, null, null, null);

        service.update(5L, req);

        assertThat(existing.getName()).isEqualTo("New Name");
    }

    private Client clientWithId(final Long id, final String name) {
        final Client c = new Client();
        c.setId(id);
        c.setTenantId(2L);
        c.setName(name);
        c.setStatus(ClientStatus.ACTIVE);
        c.setCreatedAt(OffsetDateTime.now());
        return c;
    }
}
