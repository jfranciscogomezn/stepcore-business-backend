package com.stepcore.business.operations.service;

import com.stepcore.business.exception.OsiNotFoundException;
import com.stepcore.business.operations.controller.dto.PortalOsiResponse;
import com.stepcore.business.operations.domain.model.Client;
import com.stepcore.business.operations.domain.model.Osi;
import com.stepcore.business.operations.domain.model.OsiPortalAccessLog;
import com.stepcore.business.operations.domain.model.OsiTrackingToken;
import com.stepcore.business.operations.domain.model.OsiVehicleAssignment;
import com.stepcore.business.operations.domain.model.OsiVehicleState;
import com.stepcore.business.operations.repository.ClientRepository;
import com.stepcore.business.operations.repository.OsiPortalAccessLogRepository;
import com.stepcore.business.operations.repository.OsiRepository;
import com.stepcore.business.operations.repository.OsiTrackingTokenRepository;
import com.stepcore.business.operations.repository.OsiVehicleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicPortalServiceImpl implements PublicPortalService {

    private final OsiTrackingTokenRepository tokenRepository;
    private final OsiPortalAccessLogRepository accessLogRepository;
    private final OsiRepository osiRepository;
    private final ClientRepository clientRepository;
    private final OsiVehicleAssignmentRepository assignmentRepository;
    private final OsiEventService osiEventService;

    @Override
    @Transactional
    public PortalOsiResponse getPortalData(final UUID token, final String remoteIp) {
        final OsiTrackingToken trackingToken = tokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portal link not found or expired"));

        accessLogRepository.save(OsiPortalAccessLog.builder()
                .withTokenId(trackingToken.getId())
                .withAccessedAt(OffsetDateTime.now())
                .withIpHash(hashIp(remoteIp))
                .build());

        final Osi osi = osiRepository.findById(trackingToken.getOsiId())
                .orElseThrow(() -> new OsiNotFoundException(trackingToken.getOsiId()));

        final String clientName = clientRepository.findById(osi.getClientId())
                .map(Client::getName).orElse("—");

        final String aggregatedState = deriveAggregatedState(
                assignmentRepository.findByOsiId(osi.getId()));

        return new PortalOsiResponse(
                osi.getOsiNumber(),
                clientName,
                osi.getOrigin(),
                osi.getDestination(),
                aggregatedState,
                osiEventService.listForPortal(osi.getId()));
    }

    private String deriveAggregatedState(final List<OsiVehicleAssignment> assignments) {
        if (assignments.isEmpty()) return "DRAFT";
        return assignments.stream()
                .map(OsiVehicleAssignment::getState)
                .max(Comparator.comparing(Enum::ordinal))
                .map(OsiVehicleState::name)
                .orElse("PLANNED");
    }

    private String hashIp(final String ip) {
        if (ip == null || ip.isBlank()) return "unknown";
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] hash = md.digest(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return "hash-error";
        }
    }
}
