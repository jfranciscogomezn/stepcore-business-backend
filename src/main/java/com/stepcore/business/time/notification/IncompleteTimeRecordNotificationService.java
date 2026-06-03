package com.stepcore.business.time.notification;

import com.stepcore.business.time.domain.model.TimeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class IncompleteTimeRecordNotificationService {

    public void notifyAdminsOfIncompleteRecords(final List<TimeRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        log.warn(
                "[IncompleteTimeRecordNotificationService] - NOTIFY: {} record(s) marked INCOMPLETE; "
                        + "in-app and email delivery pending integration",
                records.size());
        records.forEach(record -> log.warn(
                "[IncompleteTimeRecordNotificationService] - RECORD: employeeId={} date={}",
                record.getEmployeeId(),
                record.getWorkDate()));
    }
}
