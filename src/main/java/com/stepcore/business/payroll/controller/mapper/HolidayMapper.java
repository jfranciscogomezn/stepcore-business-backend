package com.stepcore.business.payroll.controller.mapper;

import com.stepcore.business.payroll.controller.dto.HolidayRequest;
import com.stepcore.business.payroll.controller.dto.HolidayResponse;
import com.stepcore.business.payroll.domain.model.Holiday;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface HolidayMapper {

    @Mapping(source = "holidayDate", target = "date")
    HolidayResponse toResponse(Holiday entity);

    @Mapping(source = "date", target = "holidayDate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Holiday toEntity(HolidayRequest request);
}
