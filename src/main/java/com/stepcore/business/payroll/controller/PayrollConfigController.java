package com.stepcore.business.payroll.controller;

import com.stepcore.business.payroll.controller.dto.PayrollConfigRequest;
import com.stepcore.business.payroll.controller.dto.PayrollConfigResponse;
import com.stepcore.business.payroll.service.PayrollConfigService;
import com.stepcore.business.security.AppPermissions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config/payroll")
@RequiredArgsConstructor
public class PayrollConfigController {

    private final PayrollConfigService payrollConfigService;

    @GetMapping("/{year}")
    @PreAuthorize("hasAuthority('" + AppPermissions.PAYROLL_CONFIG + "')")
    public PayrollConfigResponse getByYear(@PathVariable final int year) {
        return payrollConfigService.getByYear(year);
    }

    @PutMapping("/{year}")
    @PreAuthorize("hasAuthority('" + AppPermissions.PAYROLL_CONFIG + "')")
    public ResponseEntity<PayrollConfigResponse> upsert(
            @PathVariable final int year,
            @Valid @RequestBody final PayrollConfigRequest request) {
        final PayrollConfigService.UpsertResult result = payrollConfigService.upsert(year, request);
        return ResponseEntity.status(result.created() ? 201 : 200).body(result.body());
    }
}
