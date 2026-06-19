package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.OsiPortalAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OsiPortalAccessLogRepository extends JpaRepository<OsiPortalAccessLog, Long> {
}
