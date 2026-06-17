package com.stepcore.business.operations.service;

import com.stepcore.business.exception.DuplicateVehiclePlateException;
import com.stepcore.business.exception.VehicleRetiredException;
import com.stepcore.business.operations.controller.dto.CreateVehicleRequest;
import com.stepcore.business.operations.controller.dto.UpdateVehicleRequest;
import com.stepcore.business.operations.controller.dto.VehicleResponse;
import com.stepcore.business.operations.domain.model.Vehicle;
import com.stepcore.business.operations.domain.model.VehicleStatus;
import com.stepcore.business.operations.domain.model.VehicleType;
import com.stepcore.business.operations.repository.VehicleRepository;
import com.stepcore.business.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    private VehicleServiceImpl service;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(2L);
        service = new VehicleServiceImpl(vehicleRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_normalisesPlate() {
        final CreateVehicleRequest req = new CreateVehicleRequest("abc 123", null, null, null, null, null);
        when(vehicleRepository.findByTenantIdAndPlate(2L, "ABC123")).thenReturn(Optional.empty());
        final Vehicle saved = vehicleWithId(1L, "ABC123");
        when(vehicleRepository.save(any())).thenReturn(saved);

        final VehicleResponse result = service.create(req);

        assertThat(result.plate()).isEqualTo("ABC123");
    }

    @Test
    void create_duplicatePlate_throwsConflict() {
        when(vehicleRepository.findByTenantIdAndPlate(2L, "ABC123"))
                .thenReturn(Optional.of(vehicleWithId(1L, "ABC123")));
        final CreateVehicleRequest req = new CreateVehicleRequest("ABC123", null, null, null, null, null);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(DuplicateVehiclePlateException.class);
    }

    @Test
    void findById_notFound_throws() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(com.stepcore.business.exception.VehicleNotFoundException.class);
    }

    @Test
    void update_changesStatus() {
        final Vehicle v = vehicleWithId(3L, "XYZ789");
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(v));
        when(vehicleRepository.save(any())).thenReturn(v);
        final UpdateVehicleRequest req = new UpdateVehicleRequest(null, null, null, null, "MAINTENANCE", null);

        service.update(3L, req);

        assertThat(v.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
    }

    @Test
    void retiredVehicleStatus_isAvailableForQuery() {
        final Vehicle retired = vehicleWithId(5L, "RET001");
        retired.setStatus(VehicleStatus.RETIRED);
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(retired));

        final VehicleResponse result = service.findById(5L);

        assertThat(result.status()).isEqualTo("RETIRED");
    }

    private Vehicle vehicleWithId(final Long id, final String plate) {
        final Vehicle v = new Vehicle();
        v.setId(id);
        v.setTenantId(2L);
        v.setPlate(plate);
        v.setType(VehicleType.CAMION);
        v.setStatus(VehicleStatus.ACTIVE);
        v.setCreatedAt(OffsetDateTime.now());
        return v;
    }
}
