package com.stepcore.business.payroll.service;

import com.stepcore.business.exception.PayrollConfigNotFoundException;
import com.stepcore.business.payroll.controller.dto.PayrollConfigRequest;
import com.stepcore.business.payroll.controller.dto.PayrollConfigResponse;
import com.stepcore.business.payroll.controller.mapper.PayrollConfigMapper;
import com.stepcore.business.payroll.domain.model.PayrollConfig;
import com.stepcore.business.payroll.repository.PayrollConfigRepository;
import com.stepcore.business.support.PayrollConfigTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollConfigServiceTest {

    @Mock private PayrollConfigRepository payrollConfigRepository;
    @Mock private PayrollConfigMapper payrollConfigMapper;

    @InjectMocks private PayrollConfigServiceImpl payrollConfigService;

    @Test
    void shouldCreateConfigurationWhenYearDoesNotExist() {
        final PayrollConfigRequest request = PayrollConfigTestSupport.validRequest();
        final PayrollConfig mapped = PayrollConfig.builder().withMinimumWage(request.minimumWage()).build();
        final PayrollConfig saved = PayrollConfig.builder().withYear(2026).withMinimumWage(request.minimumWage()).build();
        final PayrollConfigResponse response = new PayrollConfigResponse(
                2026, request.minimumWage(), request.transportSubsidy(), request.monthlyWorkHours(),
                request.normalDailyHours(), request.maxDailyExtraHours(), request.daytimeStart(),
                request.daytimeEnd(), request.daytimeOtStart(), request.daytimeOtEnd(),
                request.nightSurchargeStart(), request.nightSurchargeEnd(), request.nocturnalOtStart(),
                request.nocturnalOtEnd(), request.sundayOtStart(), request.sundayOtEnd(),
                request.daytimeOtFactor(), request.nocturnalOtFactor(), request.nightSurchargeFactor(),
                request.sundayHolidayDaytimeOtFactor(), request.sundayHolidayNocturnalOtFactor(),
                request.sundayHolidayNormalFactor(), request.nonBillableRestMinutes());

        when(payrollConfigRepository.findByYear(2026)).thenReturn(Optional.empty());
        when(payrollConfigMapper.toEntity(request)).thenReturn(mapped);
        when(payrollConfigRepository.save(any(PayrollConfig.class))).thenReturn(saved);
        when(payrollConfigMapper.toResponse(saved)).thenReturn(response);

        final PayrollConfigService.UpsertResult result = payrollConfigService.upsert(2026, request);

        assertThat(result.created()).isTrue();
        assertThat(result.body().year()).isEqualTo(2026);
        final ArgumentCaptor<PayrollConfig> captor = ArgumentCaptor.forClass(PayrollConfig.class);
        verify(payrollConfigRepository).save(captor.capture());
        assertThat(captor.getValue().getYear()).isEqualTo(2026);
    }

    @Test
    void shouldUpdateConfigurationWhenYearExists() {
        final PayrollConfigRequest request = PayrollConfigTestSupport.validRequest();
        final PayrollConfig existing = PayrollConfig.builder().withYear(2026).build();
        final PayrollConfigResponse response = new PayrollConfigResponse(
                2026, request.minimumWage(), request.transportSubsidy(), request.monthlyWorkHours(),
                request.normalDailyHours(), request.maxDailyExtraHours(), request.daytimeStart(),
                request.daytimeEnd(), request.daytimeOtStart(), request.daytimeOtEnd(),
                request.nightSurchargeStart(), request.nightSurchargeEnd(), request.nocturnalOtStart(),
                request.nocturnalOtEnd(), request.sundayOtStart(), request.sundayOtEnd(),
                request.daytimeOtFactor(), request.nocturnalOtFactor(), request.nightSurchargeFactor(),
                request.sundayHolidayDaytimeOtFactor(), request.sundayHolidayNocturnalOtFactor(),
                request.sundayHolidayNormalFactor(), request.nonBillableRestMinutes());

        when(payrollConfigRepository.findByYear(2026)).thenReturn(Optional.of(existing));
        when(payrollConfigRepository.save(existing)).thenReturn(existing);
        when(payrollConfigMapper.toResponse(existing)).thenReturn(response);

        final PayrollConfigService.UpsertResult result = payrollConfigService.upsert(2026, request);

        assertThat(result.created()).isFalse();
        verify(payrollConfigMapper).updateEntity(existing, request);
    }

    @Test
    void shouldThrowWhenConfigurationMissing() {
        when(payrollConfigRepository.findByYear(2025)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> payrollConfigService.getByYear(2025))
                .isInstanceOf(PayrollConfigNotFoundException.class);
    }
}
