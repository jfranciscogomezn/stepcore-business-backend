package com.stepcore.business.operations.controller;

import com.stepcore.business.operations.controller.dto.TrackingTokenResponse;
import com.stepcore.business.operations.service.OsiTrackingTokenService;
import com.stepcore.business.operations.service.UserResolver;
import com.stepcore.business.security.AppPermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations/osi/{osiId}/token")
@RequiredArgsConstructor
public class OsiTrackingTokenController {

    private final OsiTrackingTokenService tokenService;
    private final UserResolver userResolver;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public ResponseEntity<TrackingTokenResponse> getActive(@PathVariable Long osiId) {
        return tokenService.findActive(osiId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public ResponseEntity<TrackingTokenResponse> generate(
            @PathVariable Long osiId, Authentication authentication) {
        final Long userId = userResolver.resolveByEmail(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tokenService.generateOrReplace(osiId, userId));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public ResponseEntity<Void> revoke(@PathVariable Long osiId) {
        tokenService.revoke(osiId);
        return ResponseEntity.noContent().build();
    }
}
