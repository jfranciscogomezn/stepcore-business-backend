package com.stepcore.business.audit;

import com.stepcore.business.audit.model.TimeRecordAuditSnapshot;
import com.stepcore.business.time.domain.model.TimeRecord;

public final class TimeRecordAuditSnapshots {

    private TimeRecordAuditSnapshots() {
    }

    public static TimeRecordAuditSnapshot fromRecord(final TimeRecord record) {
        return new TimeRecordAuditSnapshot(
                record.getEmployeeId(),
                record.getWorkDate(),
                record.getClockIn(),
                record.getClockOut());
    }
}
