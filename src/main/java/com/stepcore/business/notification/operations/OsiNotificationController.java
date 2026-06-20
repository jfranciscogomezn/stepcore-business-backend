package com.stepcore.business.notification.operations;

import com.stepcore.business.security.AppPermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operations/notifications")
@RequiredArgsConstructor
public class OsiNotificationController {

    private final OsiNotificationService osiNotificationService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public List<OsiNotificationResponse> listRecent() {
        return osiNotificationService.listRecent();
    }
}
