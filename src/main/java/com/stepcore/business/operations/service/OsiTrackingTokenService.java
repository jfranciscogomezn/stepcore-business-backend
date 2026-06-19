package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.TrackingTokenResponse;

import java.util.Optional;

public interface OsiTrackingTokenService {

    TrackingTokenResponse generateOrReplace(Long osiId, Long userId);

    void revoke(Long osiId);

    Optional<TrackingTokenResponse> findActive(Long osiId);
}
