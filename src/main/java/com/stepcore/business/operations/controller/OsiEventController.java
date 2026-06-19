package com.stepcore.business.operations.controller;

import com.stepcore.business.operations.controller.dto.AddAttachmentRequest;
import com.stepcore.business.operations.controller.dto.AddCommentRequest;
import com.stepcore.business.operations.controller.dto.CreateCorrectiveEventRequest;
import com.stepcore.business.operations.controller.dto.CreateOsiEventRequest;
import com.stepcore.business.operations.controller.dto.OsiEventResponse;
import com.stepcore.business.operations.service.OsiEventService;
import com.stepcore.business.operations.service.UserResolver;
import com.stepcore.business.security.AppPermissions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/osi/{osiId}/vehicles/{vehicleId}/events")
@RequiredArgsConstructor
public class OsiEventController {

    private final OsiEventService osiEventService;
    private final UserResolver userResolver;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public List<OsiEventResponse> list(@PathVariable Long osiId, @PathVariable Long vehicleId) {
        return osiEventService.list(osiId, vehicleId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public ResponseEntity<OsiEventResponse> create(
            @PathVariable Long osiId,
            @PathVariable Long vehicleId,
            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey,
            @Valid @RequestBody CreateOsiEventRequest request,
            Authentication authentication) {
        final Long userId = userResolver.resolveByEmail(authentication.getName());
        final UUID key = idempotencyKey != null ? idempotencyKey : UUID.randomUUID();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(osiEventService.create(osiId, vehicleId, request, userId, key));
    }

    @PostMapping("/{eventId}/correct")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public ResponseEntity<OsiEventResponse> correct(
            @PathVariable Long osiId,
            @PathVariable Long vehicleId,
            @PathVariable Long eventId,
            @Valid @RequestBody CreateCorrectiveEventRequest request,
            Authentication authentication) {
        final Long userId = userResolver.resolveByEmail(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(osiEventService.createCorrective(osiId, vehicleId, eventId, request, userId));
    }

    @PostMapping("/{eventId}/approve")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public OsiEventResponse approve(
            @PathVariable Long osiId,
            @PathVariable Long vehicleId,
            @PathVariable Long eventId,
            Authentication authentication) {
        final Long userId = userResolver.resolveByEmail(authentication.getName());
        return osiEventService.approveVisibility(osiId, vehicleId, eventId, userId);
    }

    @PostMapping("/{eventId}/attachments")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public OsiEventResponse addAttachment(
            @PathVariable Long osiId,
            @PathVariable Long vehicleId,
            @PathVariable Long eventId,
            @Valid @RequestBody AddAttachmentRequest request) {
        return osiEventService.addAttachment(osiId, vehicleId, eventId, request);
    }

    @PostMapping("/{eventId}/comments")
    @PreAuthorize("hasAnyAuthority('" + AppPermissions.OPS_OSI + "','" + AppPermissions.OPS_COMERCIAL + "')")
    public OsiEventResponse addComment(
            @PathVariable Long osiId,
            @PathVariable Long vehicleId,
            @PathVariable Long eventId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {
        final Long userId = userResolver.resolveByEmail(authentication.getName());
        return osiEventService.addComment(osiId, vehicleId, eventId, request, userId);
    }
}
