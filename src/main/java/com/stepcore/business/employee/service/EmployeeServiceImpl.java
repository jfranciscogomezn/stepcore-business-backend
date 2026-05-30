package com.stepcore.business.employee.service;

import com.stepcore.business.employee.controller.dto.CreateEmployeeRequest;
import com.stepcore.business.employee.controller.dto.EmployeeResponse;
import com.stepcore.business.employee.controller.dto.UpdateEmployeeRequest;
import com.stepcore.business.employee.controller.mapper.EmployeeMapper;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.exception.DuplicateEmployeeDocumentException;
import com.stepcore.business.exception.DuplicateEmployeeEmailException;
import com.stepcore.business.exception.EmployeeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponse create(final CreateEmployeeRequest request) {
        assertUniqueDocument(request.idNumber(), null);
        assertUniqueEmail(request.email(), null);
        return employeeMapper.toResponse(employeeRepository.save(employeeMapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> listAll() {
        return employeeRepository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(final Long id) {
        return employeeRepository.findById(id)
                .map(employeeMapper::toResponse)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @Override
    public EmployeeResponse update(final Long id, final UpdateEmployeeRequest request) {
        final var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        assertUniqueDocument(request.idNumber(), id);
        assertUniqueEmail(request.email(), id);
        employeeMapper.updateEntity(employee, request);
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    private void assertUniqueDocument(final String idNumber, final Long excludeId) {
        final boolean exists = excludeId == null
                ? employeeRepository.existsByIdNumber(idNumber)
                : employeeRepository.existsByIdNumberAndIdNot(idNumber, excludeId);
        if (exists) {
            throw new DuplicateEmployeeDocumentException(idNumber);
        }
    }

    private void assertUniqueEmail(final String email, final Long excludeId) {
        final boolean exists = excludeId == null
                ? employeeRepository.existsByEmail(email)
                : employeeRepository.existsByEmailAndIdNot(email, excludeId);
        if (exists) {
            throw new DuplicateEmployeeEmailException(email);
        }
    }
}
