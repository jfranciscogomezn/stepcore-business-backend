package com.stepcore.business.time.repository;

import com.stepcore.business.time.domain.model.TimeRecord;
import com.stepcore.business.time.domain.model.TimeRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

    Optional<TimeRecord> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    List<TimeRecord> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(
            Long employeeId, LocalDate from, LocalDate to);

    List<TimeRecord> findByEmployeeIdAndStatusOrderByWorkDateDesc(
            Long employeeId, TimeRecordStatus status);

    List<TimeRecord> findByStatusAndWorkDateBeforeOrderByWorkDateAsc(
            TimeRecordStatus status, LocalDate workDate);

    List<TimeRecord> findByStatusOrderByWorkDateDesc(TimeRecordStatus status);
}
