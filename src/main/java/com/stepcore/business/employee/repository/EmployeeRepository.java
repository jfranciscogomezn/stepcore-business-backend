package com.stepcore.business.employee.repository;

import com.stepcore.business.employee.domain.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findAllByOrderByLastNameAscFirstNameAsc();

    boolean existsByIdNumber(String idNumber);

    boolean existsByEmail(String email);

    boolean existsByIdNumberAndIdNot(String idNumber, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);
}
