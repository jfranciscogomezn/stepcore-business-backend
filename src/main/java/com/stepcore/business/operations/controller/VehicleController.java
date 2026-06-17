package com.stepcore.business.operations.controller;

import com.stepcore.business.operations.controller.dto.CreateVehicleRequest;
import com.stepcore.business.operations.controller.dto.UpdateVehicleRequest;
import com.stepcore.business.operations.controller.dto.VehicleResponse;
import com.stepcore.business.operations.service.VehicleService;
import com.stepcore.business.security.AppPermissions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/operations/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_VEHICLES + "')")
    public List<VehicleResponse> findAll(@RequestParam(required = false) String status) {
        return vehicleService.findAll(status);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_VEHICLES + "')")
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody CreateVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_VEHICLES + "')")
    public VehicleResponse findById(@PathVariable Long id) {
        return vehicleService.findById(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_VEHICLES + "')")
    public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody UpdateVehicleRequest request) {
        return vehicleService.update(id, request);
    }
}
