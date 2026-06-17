package com.stepcore.business.time.repository;

import com.stepcore.business.time.domain.model.TimeCorrectionRequest;
import com.stepcore.business.time.domain.model.TimeCorrectionRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeCorrectionRequestRepository extends JpaRepository<TimeCorrectionRequest, Long> {

    Optional<TimeCorrectionRequest> findByTimeRecordIdAndStatus(Long timeRecordId, TimeCorrectionRequestStatus status);

    boolean existsByTimeRecordIdAndStatus(Long timeRecordId, TimeCorrectionRequestStatus status);

    List<TimeCorrectionRequest> findByTenantIdAndStatusOrderByCreatedAtAsc(Long tenantId, TimeCorrectionRequestStatus status);
}
