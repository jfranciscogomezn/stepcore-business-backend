package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {

    List<EventType> findByTenantIdOrderByNameAsc(Long tenantId);

    List<EventType> findByTenantIdAndActiveOrderByNameAsc(Long tenantId, boolean active);
}
