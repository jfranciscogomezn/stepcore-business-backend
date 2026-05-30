package com.stepcore.business.payroll.service;

import com.stepcore.business.exception.DuplicateHolidayException;
import com.stepcore.business.exception.HolidayNotFoundException;
import com.stepcore.business.payroll.controller.dto.HolidayRequest;
import com.stepcore.business.payroll.controller.dto.HolidayResponse;
import com.stepcore.business.payroll.controller.mapper.HolidayMapper;
import com.stepcore.business.payroll.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final HolidayMapper holidayMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> listByYear(final int year) {
        final LocalDate start = LocalDate.of(year, 1, 1);
        final LocalDate end = LocalDate.of(year, 12, 31);
        return holidayRepository.findByHolidayDateBetweenOrderByHolidayDateAsc(start, end).stream()
                .map(holidayMapper::toResponse)
                .toList();
    }

    @Override
    public HolidayResponse create(final HolidayRequest request) {
        if (holidayRepository.existsByHolidayDate(request.date())) {
            throw new DuplicateHolidayException(request.date());
        }
        return holidayMapper.toResponse(holidayRepository.save(holidayMapper.toEntity(request)));
    }

    @Override
    public void delete(final Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new HolidayNotFoundException(id);
        }
        holidayRepository.deleteById(id);
    }
}
