package com.stepcore.business.operations.controller;

import com.stepcore.business.operations.controller.dto.ChangeOsiOwnerRequest;
import com.stepcore.business.operations.controller.dto.CreateOsiRequest;
import com.stepcore.business.operations.controller.dto.OsiResponse;
import com.stepcore.business.operations.controller.dto.OsiSummaryResponse;
import com.stepcore.business.operations.controller.dto.UpdateOsiRequest;
import com.stepcore.business.operations.service.DigestService;
import com.stepcore.business.operations.service.OsiService;
import com.stepcore.business.operations.service.UserResolver;
import com.stepcore.business.security.AppPermissions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

@RestController
@RequestMapping("/api/v1/operations/osi")
@RequiredArgsConstructor
public class OsiController {

    private final OsiService osiService;
    private final DigestService digestService;
    private final UserResolver userResolver;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public Page<OsiSummaryResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @PageableDefault(size = 20) Pageable pageable) {
        return osiService.list(status, dateFrom, dateTo, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public ResponseEntity<OsiResponse> create(
            @Valid @RequestBody CreateOsiRequest request,
            Authentication authentication) {
        final Long userId = userResolver.resolveByEmail(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(osiService.create(request, userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public OsiResponse findById(@PathVariable Long id) {
        return osiService.findById(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public OsiResponse update(@PathVariable Long id, @Valid @RequestBody UpdateOsiRequest request) {
        return osiService.update(id, request);
    }

    @PatchMapping("/{id}/owner")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public OsiResponse changeOwner(@PathVariable Long id, @Valid @RequestBody ChangeOsiOwnerRequest request) {
        return osiService.changeOwner(id, request);
    }

    @GetMapping(value = "/{id}/digest", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public ResponseEntity<String> getDigest(@PathVariable Long id) {
        return ResponseEntity.ok(digestService.generate(id));
    }
}
