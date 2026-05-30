package com.stepcore.business.payroll.service;

import com.stepcore.business.exception.PayrollConfigNotFoundException;
import com.stepcore.business.payroll.controller.dto.PayrollConfigRequest;
import com.stepcore.business.payroll.controller.dto.PayrollConfigResponse;
import com.stepcore.business.payroll.controller.mapper.PayrollConfigMapper;
import com.stepcore.business.payroll.domain.model.PayrollConfig;
import com.stepcore.business.payroll.repository.PayrollConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollConfigServiceImpl implements PayrollConfigService {

    private final PayrollConfigRepository payrollConfigRepository;
    private final PayrollConfigMapper payrollConfigMapper;

    @Override
    @Transactional(readOnly = true)
    public PayrollConfigResponse getByYear(final int year) {
        return payrollConfigRepository.findByYear(year)
                .map(payrollConfigMapper::toResponse)
                .orElseThrow(() -> new PayrollConfigNotFoundException(year));
    }

    @Override
    public UpsertResult upsert(final int year, final PayrollConfigRequest request) {
        return payrollConfigRepository.findByYear(year)
                .map(existing -> updateExisting(existing, request))
                .orElseGet(() -> createNew(year, request));
    }

    private UpsertResult updateExisting(final PayrollConfig existing, final PayrollConfigRequest request) {
        payrollConfigMapper.updateEntity(existing, request);
        return new UpsertResult(payrollConfigMapper.toResponse(payrollConfigRepository.save(existing)), false);
    }

    private UpsertResult createNew(final int year, final PayrollConfigRequest request) {
        final PayrollConfig created = payrollConfigMapper.toEntity(request);
        final PayrollConfig persisted = payrollConfigRepository.save(
                PayrollConfig.builder()
                        .withYear(year)
                        .withMinimumWage(created.getMinimumWage())
                        .withTransportSubsidy(created.getTransportSubsidy())
                        .withMonthlyWorkHours(created.getMonthlyWorkHours())
                        .withNormalDailyHours(created.getNormalDailyHours())
                        .withMaxDailyExtraHours(created.getMaxDailyExtraHours())
                        .withDaytimeStart(created.getDaytimeStart())
                        .withDaytimeEnd(created.getDaytimeEnd())
                        .withDaytimeOtStart(created.getDaytimeOtStart())
                        .withDaytimeOtEnd(created.getDaytimeOtEnd())
                        .withNightSurchargeStart(created.getNightSurchargeStart())
                        .withNightSurchargeEnd(created.getNightSurchargeEnd())
                        .withNocturnalOtStart(created.getNocturnalOtStart())
                        .withNocturnalOtEnd(created.getNocturnalOtEnd())
                        .withSundayOtStart(created.getSundayOtStart())
                        .withSundayOtEnd(created.getSundayOtEnd())
                        .withDaytimeOtFactor(created.getDaytimeOtFactor())
                        .withNocturnalOtFactor(created.getNocturnalOtFactor())
                        .withNightSurchargeFactor(created.getNightSurchargeFactor())
                        .withSundayHolidayDaytimeOtFactor(created.getSundayHolidayDaytimeOtFactor())
                        .withSundayHolidayNocturnalOtFactor(created.getSundayHolidayNocturnalOtFactor())
                        .withSundayHolidayNormalFactor(created.getSundayHolidayNormalFactor())
                        .withNonBillableRestMinutes(created.getNonBillableRestMinutes())
                        .build());
        return new UpsertResult(payrollConfigMapper.toResponse(persisted), true);
    }
}
