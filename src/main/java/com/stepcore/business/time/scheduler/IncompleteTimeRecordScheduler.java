package com.stepcore.business.time.scheduler;

import com.stepcore.business.notification.service.IncompleteTimeRecordJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncompleteTimeRecordScheduler {

    private final IncompleteTimeRecordJobService incompleteTimeRecordJobService;

    @Scheduled(cron = "${time-records.incomplete-job-cron:0 1 0 * * *}")
    public void flagIncompleteRecords() {
        incompleteTimeRecordJobService.flagStaleRecordsAndNotify();
    }
}
