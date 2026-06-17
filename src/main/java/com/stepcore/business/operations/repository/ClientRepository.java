package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.Client;
import com.stepcore.business.operations.domain.model.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT c FROM Client c WHERE c.tenantId = :tenantId AND lower(c.name) = lower(:name)")
    Optional<Client> findByTenantIdAndNameIgnoreCase(@Param("tenantId") Long tenantId, @Param("name") String name);

    List<Client> findAllByTenantIdAndStatusOrderByNameAsc(Long tenantId, ClientStatus status);

    List<Client> findAllByTenantIdOrderByNameAsc(Long tenantId);
}
