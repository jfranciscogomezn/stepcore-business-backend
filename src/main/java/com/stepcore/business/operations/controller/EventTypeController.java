package com.stepcore.business.operations.controller;

import com.stepcore.business.operations.controller.dto.CreateEventTypeRequest;
import com.stepcore.business.operations.controller.dto.EventTypeResponse;
import com.stepcore.business.operations.controller.dto.UpdateEventTypeRequest;
import com.stepcore.business.operations.service.EventTypeService;
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
@RequestMapping("/api/v1/operations/event-types")
@RequiredArgsConstructor
public class EventTypeController {

    private final EventTypeService eventTypeService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_EVENT_TYPES + "')")
    public List<EventTypeResponse> findAll(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return eventTypeService.findAll(activeOnly);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_EVENT_TYPES + "')")
    public ResponseEntity<EventTypeResponse> create(@Valid @RequestBody CreateEventTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventTypeService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_EVENT_TYPES + "')")
    public EventTypeResponse findById(@PathVariable Long id) {
        return eventTypeService.findById(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_EVENT_TYPES + "')")
    public EventTypeResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEventTypeRequest request) {
        return eventTypeService.update(id, request);
    }
}
