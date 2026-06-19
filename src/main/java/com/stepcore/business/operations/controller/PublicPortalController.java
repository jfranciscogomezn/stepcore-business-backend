package com.stepcore.business.operations.controller;

import com.stepcore.business.operations.controller.dto.PortalOsiResponse;
import com.stepcore.business.operations.service.PublicPortalService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/public/osi")
@RequiredArgsConstructor
public class PublicPortalController {

    private final PublicPortalService portalService;

    @GetMapping("/{token}")
    public PortalOsiResponse getPortal(
            @PathVariable UUID token,
            HttpServletRequest request) {
        final String remoteIp = resolveClientIp(request);
        return portalService.getPortalData(token, remoteIp);
    }

    private String resolveClientIp(final HttpServletRequest request) {
        final String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
