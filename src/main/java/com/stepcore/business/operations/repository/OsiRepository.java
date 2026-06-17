package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.Osi;
import com.stepcore.business.operations.domain.model.OsiStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface OsiRepository extends JpaRepository<Osi, Long> {

    Optional<Osi> findByTenantIdAndOsiNumber(Long tenantId, String osiNumber);

    Page<Osi> findByTenantIdAndStatus(Long tenantId, OsiStatus status, Pageable pageable);

    Page<Osi> findByTenantIdAndStatusAndCreatedAtBetween(
            Long tenantId, OsiStatus status,
            OffsetDateTime from, OffsetDateTime to,
            Pageable pageable);

    Page<Osi> findByTenantId(Long tenantId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Osi o WHERE o.tenantId = :tenantId AND o.clientId = :clientId " +
           "AND o.commercialReference = :ref AND o.status <> 'CLOSED' AND o.createdAt >= :since")
    long countDuplicateReference(@Param("tenantId") Long tenantId,
                                  @Param("clientId") Long clientId,
                                  @Param("ref") String commercialReference,
                                  @Param("since") OffsetDateTime since);

    @Query("SELECT COUNT(o) FROM Osi o WHERE o.tenantId = :tenantId AND o.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") OsiStatus status);

    List<Osi> findByTenantIdAndClientId(Long tenantId, Long clientId);
}
