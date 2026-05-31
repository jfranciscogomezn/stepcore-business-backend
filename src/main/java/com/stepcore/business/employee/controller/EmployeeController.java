package com.stepcore.business.employee.controller;

import com.stepcore.business.employee.controller.dto.CreateEmployeeRequest;
import com.stepcore.business.employee.controller.dto.EmployeeResponse;
import com.stepcore.business.employee.controller.dto.UpdateEmployeeRequest;
import com.stepcore.business.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody final CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<EmployeeResponse> listAll() {
        return employeeService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse getById(@PathVariable final Long id) {
        return employeeService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse update(
            @PathVariable final Long id,
            @Valid @RequestBody final UpdateEmployeeRequest request) {
        return employeeService.update(id, request);
    }
}
