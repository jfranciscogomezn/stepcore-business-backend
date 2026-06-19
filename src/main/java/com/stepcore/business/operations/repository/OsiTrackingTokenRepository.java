package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.OsiTrackingToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OsiTrackingTokenRepository extends JpaRepository<OsiTrackingToken, Long> {

    Optional<OsiTrackingToken> findByOsiIdAndRevokedAtIsNull(Long osiId);

    Optional<OsiTrackingToken> findByTokenAndRevokedAtIsNull(UUID token);
}
