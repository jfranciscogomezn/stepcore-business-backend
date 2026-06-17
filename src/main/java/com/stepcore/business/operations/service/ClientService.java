package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.ClientResponse;
import com.stepcore.business.operations.controller.dto.CreateClientRequest;
import com.stepcore.business.operations.controller.dto.UpdateClientRequest;

import java.util.List;

public interface ClientService {

    ClientResponse create(CreateClientRequest request);

    List<ClientResponse> findAll(boolean activeOnly);

    ClientResponse findById(Long id);

    ClientResponse update(Long id, UpdateClientRequest request);
}
