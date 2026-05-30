package com.stepcore.business.employee.service;

import com.stepcore.business.employee.controller.dto.CreateEmployeeRequest;
import com.stepcore.business.employee.controller.dto.EmployeeResponse;
import com.stepcore.business.employee.controller.dto.UpdateEmployeeRequest;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse create(CreateEmployeeRequest request);

    List<EmployeeResponse> listAll();

    EmployeeResponse getById(Long id);

    EmployeeResponse update(Long id, UpdateEmployeeRequest request);
}
