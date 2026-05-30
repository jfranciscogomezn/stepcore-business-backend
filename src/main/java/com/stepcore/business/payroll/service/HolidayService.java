package com.stepcore.business.payroll.service;

import com.stepcore.business.payroll.controller.dto.HolidayRequest;
import com.stepcore.business.payroll.controller.dto.HolidayResponse;

import java.util.List;

public interface HolidayService {

    List<HolidayResponse> listByYear(int year);

    HolidayResponse create(HolidayRequest request);

    void delete(Long id);
}
