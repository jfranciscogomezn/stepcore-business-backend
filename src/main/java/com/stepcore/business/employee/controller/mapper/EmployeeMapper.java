package com.stepcore.business.employee.controller.mapper;

import com.stepcore.business.employee.controller.dto.CreateEmployeeRequest;
import com.stepcore.business.employee.controller.dto.EmployeeResponse;
import com.stepcore.business.employee.controller.dto.UpdateEmployeeRequest;
import com.stepcore.business.employee.domain.model.Employee;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface EmployeeMapper {

    EmployeeResponse toResponse(Employee entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Employee toEntity(CreateEmployeeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Employee entity, UpdateEmployeeRequest request);
}
