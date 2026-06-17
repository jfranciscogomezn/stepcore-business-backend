package com.stepcore.business.time.controller;

import com.stepcore.business.security.AppPermissions;
import com.stepcore.business.time.controller.dto.CorrectionRequestResponse;
import com.stepcore.business.time.controller.dto.CreateCorrectionRequestRequest;
import com.stepcore.business.time.controller.dto.DismissCorrectionRequestRequest;
import com.stepcore.business.time.service.CorrectionRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CorrectionRequestController {

    private final CorrectionRequestService correctionRequestService;

    /**
     * Employee submits a correction request for one of their own CLOSED time records.
     * POST /api/v1/time-records/{id}/correction-request
     */
    @PostMapping("/time-records/{id}/correction-request")
    @PreAuthorize("hasAuthority('" + AppPermissions.MY_TIME + "')")
    public ResponseEntity<CorrectionRequestResponse> submit(
            final Authentication authentication,
            @PathVariable final Long id,
            @Valid @RequestBody final CreateCorrectionRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(correctionRequestService.submit(authentication.getName(), id, request));
    }

    /**
     * Admin dismisses a PENDING correction request.
     * PATCH /api/v1/time-correction-requests/{id}/dismiss
     */
    @PatchMapping("/time-correction-requests/{id}/dismiss")
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public CorrectionRequestResponse dismiss(
            final Authentication authentication,
            @PathVariable final Long id,
            @Valid @RequestBody final DismissCorrectionRequestRequest request) {
        return correctionRequestService.dismiss(authentication.getName(), id, request);
    }

    /**
     * Admin retrieves pending correction requests for the tenant, ordered oldest-first.
     * GET /api/v1/time-correction-requests?status=PENDING
     */
    @GetMapping("/time-correction-requests")
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public List<CorrectionRequestResponse> listPending(
            @RequestParam(defaultValue = "PENDING") final String status) {
        return correctionRequestService.listPending();
    }

    /**
     * Employee retrieves their own pending correction requests.
     * GET /api/v1/time-correction-requests/mine
     */
    @GetMapping("/time-correction-requests/mine")
    @PreAuthorize("hasAuthority('" + AppPermissions.MY_TIME + "')")
    public List<CorrectionRequestResponse> listMine(final Authentication authentication) {
        return correctionRequestService.listPendingForEmployee(authentication.getName());
    }
}
