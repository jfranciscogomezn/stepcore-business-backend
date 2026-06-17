package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.CreateVehicleRequest;
import com.stepcore.business.operations.controller.dto.UpdateVehicleRequest;
import com.stepcore.business.operations.controller.dto.VehicleResponse;

import java.util.List;

public interface VehicleService {

    VehicleResponse create(CreateVehicleRequest request);

    List<VehicleResponse> findAll(String status);

    VehicleResponse findById(Long id);

    VehicleResponse update(Long id, UpdateVehicleRequest request);
}
