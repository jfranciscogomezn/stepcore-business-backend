package com.stepcore.business.payroll.service;

import com.stepcore.business.payroll.controller.dto.PayrollConfigRequest;
import com.stepcore.business.payroll.controller.dto.PayrollConfigResponse;

public interface PayrollConfigService {

    PayrollConfigResponse getByYear(int year);

    UpsertResult upsert(int year, PayrollConfigRequest request);

    record UpsertResult(PayrollConfigResponse body, boolean created) {
    }
}
