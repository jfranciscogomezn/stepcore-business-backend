package com.stepcore.business.operations.service;

import com.stepcore.business.exception.DuplicateVehiclePlateException;
import com.stepcore.business.exception.VehicleNotFoundException;
import com.stepcore.business.operations.controller.dto.CreateVehicleRequest;
import com.stepcore.business.operations.controller.dto.UpdateVehicleRequest;
import com.stepcore.business.operations.controller.dto.VehicleResponse;
import com.stepcore.business.operations.domain.model.Vehicle;
import com.stepcore.business.operations.domain.model.VehicleStatus;
import com.stepcore.business.operations.domain.model.VehicleType;
import com.stepcore.business.operations.repository.VehicleRepository;
import com.stepcore.business.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public VehicleResponse create(final CreateVehicleRequest request) {
        final Long tenantId = TenantContext.getTenantIdOrDefault();
        final String normalised = normalisePlate(request.plate());
        vehicleRepository.findByTenantIdAndPlate(tenantId, normalised)
                .ifPresent(v -> { throw new DuplicateVehiclePlateException(normalised); });

        final Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .withPlate(normalised)
                .withType(parseType(request.type()))
                .withBrand(request.brand())
                .withModel(request.model())
                .withYear(request.year())
                .withInternalNotes(request.internalNotes())
                .withStatus(VehicleStatus.ACTIVE)
                .build());
        return toResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll(final String status) {
        final Long tenantId = TenantContext.getTenantIdOrDefault();
        if (status != null && !status.isBlank()) {
            return vehicleRepository
                    .findAllByTenantIdAndStatusOrderByPlateAsc(tenantId, VehicleStatus.valueOf(status))
                    .stream().map(this::toResponse).toList();
        }
        return vehicleRepository.findAllByTenantIdOrderByPlateAsc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse findById(final Long id) {
        return toResponse(fetchOrThrow(id));
    }

    @Override
    @Transactional
    public VehicleResponse update(final Long id, final UpdateVehicleRequest request) {
        final Vehicle vehicle = fetchOrThrow(id);
        if (request.type() != null)         vehicle.setType(parseType(request.type()));
        if (request.brand() != null)        vehicle.setBrand(request.brand());
        if (request.model() != null)        vehicle.setModel(request.model());
        if (request.year() != null)         vehicle.setYear(request.year());
        if (request.status() != null)       vehicle.setStatus(VehicleStatus.valueOf(request.status()));
        if (request.internalNotes() != null) vehicle.setInternalNotes(request.internalNotes());
        return toResponse(vehicleRepository.save(vehicle));
    }

    private Vehicle fetchOrThrow(final Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    private String normalisePlate(final String plate) {
        return plate.toUpperCase().replaceAll("\\s+", "");
    }

    private VehicleType parseType(final String type) {
        if (type == null) return VehicleType.CAMION;
        try {
            return VehicleType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return VehicleType.OTRO;
        }
    }

    private VehicleResponse toResponse(final Vehicle v) {
        return new VehicleResponse(
                v.getId(), v.getPlate(), v.getType().name(),
                v.getBrand(), v.getModel(), v.getYear(),
                v.getStatus().name(), v.getInternalNotes(), v.getCreatedAt());
    }
}
