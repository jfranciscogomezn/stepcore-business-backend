package com.stepcore.business.employee.service;

import com.stepcore.business.employee.controller.dto.CreateEmployeeRequest;
import com.stepcore.business.employee.controller.dto.EmployeeResponse;
import com.stepcore.business.employee.controller.dto.UpdateEmployeeRequest;
import com.stepcore.business.employee.controller.mapper.EmployeeMapper;
import com.stepcore.business.employee.domain.model.Employee;
import com.stepcore.business.employee.domain.model.IdType;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.exception.DuplicateEmployeeDocumentException;
import com.stepcore.business.exception.EmployeeNotFoundException;
import com.stepcore.business.support.EmployeeTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeMapper employeeMapper;

    @InjectMocks private EmployeeServiceImpl employeeService;

    @Test
    void shouldCreateEmployee() {
        final CreateEmployeeRequest request = EmployeeTestSupport.validCreateRequest();
        final Employee mapped = Employee.builder().withFirstName(request.firstName()).build();
        final Employee saved = Employee.builder().withId(1L).withFirstName(request.firstName()).build();
        final EmployeeResponse response = new EmployeeResponse(
                1L, request.firstName(), request.lastName(), request.idType(), request.idNumber(),
                request.email(), request.phone(), request.monthlySalary(), request.userId());

        when(employeeRepository.existsByIdNumber(request.idNumber())).thenReturn(false);
        when(employeeRepository.existsByEmail(request.email())).thenReturn(false);
        when(employeeMapper.toEntity(request)).thenReturn(mapped);
        when(employeeRepository.save(mapped)).thenReturn(saved);
        when(employeeMapper.toResponse(saved)).thenReturn(response);

        final EmployeeResponse result = employeeService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("Ana");
    }

    @Test
    void shouldRejectDuplicateDocumentNumber() {
        final CreateEmployeeRequest request = EmployeeTestSupport.validCreateRequest();
        when(employeeRepository.existsByIdNumber(request.idNumber())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(DuplicateEmployeeDocumentException.class);
    }

    @Test
    void shouldListEmployees() {
        final Employee employee = Employee.builder().withId(1L).build();
        final EmployeeResponse response = new EmployeeResponse(
                1L, "Ana", "García", IdType.CC, "123", "a@x.com", null, BigDecimal.TEN, null);

        when(employeeRepository.findAllByOrderByLastNameAscFirstNameAsc()).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        assertThat(employeeService.listAll()).hasSize(1);
    }

    @Test
    void shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.getById(99L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void shouldUpdateEmployee() {
        final Employee existing = Employee.builder().withId(1L).withIdNumber("old").build();
        final UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "Ana", "López", IdType.CC, "999", "new@example.com", "300", BigDecimal.valueOf(4000000), null);
        final EmployeeResponse response = new EmployeeResponse(
                1L, request.firstName(), request.lastName(), request.idType(), request.idNumber(),
                request.email(), request.phone(), request.monthlySalary(), request.userId());

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByIdNumberAndIdNot(request.idNumber(), 1L)).thenReturn(false);
        when(employeeRepository.existsByEmailAndIdNot(request.email(), 1L)).thenReturn(false);
        when(employeeRepository.save(existing)).thenReturn(existing);
        when(employeeMapper.toResponse(existing)).thenReturn(response);

        final EmployeeResponse result = employeeService.update(1L, request);

        assertThat(result.lastName()).isEqualTo("López");
        verify(employeeMapper).updateEntity(existing, request);
    }
}
