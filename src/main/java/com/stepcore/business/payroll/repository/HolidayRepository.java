package com.stepcore.business.payroll.repository;

import com.stepcore.business.payroll.domain.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByHolidayDateBetweenOrderByHolidayDateAsc(LocalDate start, LocalDate end);

    boolean existsByHolidayDate(LocalDate holidayDate);
}
