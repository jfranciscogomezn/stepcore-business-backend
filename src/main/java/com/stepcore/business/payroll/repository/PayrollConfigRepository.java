package com.stepcore.business.payroll.repository;

import com.stepcore.business.payroll.domain.model.PayrollConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollConfigRepository extends JpaRepository<PayrollConfig, Long> {

    Optional<PayrollConfig> findByYear(int year);
}
