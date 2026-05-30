package com.stepcore.business.payroll.controller.mapper;

import com.stepcore.business.payroll.controller.dto.PayrollConfigRequest;
import com.stepcore.business.payroll.controller.dto.PayrollConfigResponse;
import com.stepcore.business.payroll.domain.model.PayrollConfig;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PayrollConfigMapper {

    PayrollConfigResponse toResponse(PayrollConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PayrollConfig toEntity(PayrollConfigRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget PayrollConfig entity, PayrollConfigRequest request);
}
