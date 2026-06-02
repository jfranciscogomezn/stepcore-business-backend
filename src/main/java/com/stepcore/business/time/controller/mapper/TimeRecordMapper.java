package com.stepcore.business.time.controller.mapper;

import com.stepcore.business.time.controller.dto.TimeRecordResponse;
import com.stepcore.business.time.domain.model.TimeRecord;
import org.springframework.stereotype.Component;

@Component
public class TimeRecordMapper {

    public TimeRecordResponse toResponse(final TimeRecord record) {
        return new TimeRecordResponse(
                record.getId(),
                record.getEmployeeId(),
                record.getWorkDate(),
                record.getClockIn(),
                record.getClockOut(),
                record.getStatus(),
                record.isCorrected());
    }
}
