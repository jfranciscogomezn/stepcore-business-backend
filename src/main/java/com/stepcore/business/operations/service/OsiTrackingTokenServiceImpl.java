package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.TrackingTokenResponse;
import com.stepcore.business.operations.domain.model.OsiTrackingToken;
import com.stepcore.business.operations.repository.OsiTrackingTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OsiTrackingTokenServiceImpl implements OsiTrackingTokenService {

    private final OsiTrackingTokenRepository tokenRepository;

    @Override
    @Transactional
    public TrackingTokenResponse generateOrReplace(final Long osiId, final Long userId) {
        tokenRepository.findByOsiIdAndRevokedAtIsNull(osiId).ifPresent(existing -> {
            existing.setRevokedAt(OffsetDateTime.now());
            tokenRepository.save(existing);
        });

        final OsiTrackingToken token = tokenRepository.save(
                OsiTrackingToken.builder()
                        .withOsiId(osiId)
                        .withToken(UUID.randomUUID())
                        .withCreatedByUserId(userId)
                        .build());
        return toResponse(token);
    }

    @Override
    @Transactional
    public void revoke(final Long osiId) {
        tokenRepository.findByOsiIdAndRevokedAtIsNull(osiId).ifPresent(t -> {
            t.setRevokedAt(OffsetDateTime.now());
            tokenRepository.save(t);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrackingTokenResponse> findActive(final Long osiId) {
        return tokenRepository.findByOsiIdAndRevokedAtIsNull(osiId).map(this::toResponse);
    }

    private TrackingTokenResponse toResponse(final OsiTrackingToken t) {
        return new TrackingTokenResponse(
                t.getId(), t.getOsiId(), t.getToken(),
                t.getCreatedByUserId(), t.getCreatedAt(), t.getRevokedAt());
    }
}
