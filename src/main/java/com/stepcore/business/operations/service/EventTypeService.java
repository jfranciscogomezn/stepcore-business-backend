package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.CreateEventTypeRequest;
import com.stepcore.business.operations.controller.dto.EventTypeResponse;
import com.stepcore.business.operations.controller.dto.UpdateEventTypeRequest;

import java.util.List;

public interface EventTypeService {

    EventTypeResponse create(CreateEventTypeRequest request);

    List<EventTypeResponse> findAll(boolean activeOnly);

    EventTypeResponse findById(Long id);

    EventTypeResponse update(Long id, UpdateEventTypeRequest request);
}
