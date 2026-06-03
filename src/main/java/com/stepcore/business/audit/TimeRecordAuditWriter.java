package com.stepcore.business.audit;

import com.stepcore.business.audit.model.TimeRecordAuditAction;
import com.stepcore.business.audit.model.TimeRecordAuditSnapshot;

public interface TimeRecordAuditWriter {

    void logChange(
            String actorEmail,
            TimeRecordAuditAction action,
            Long timeRecordId,
            TimeRecordAuditSnapshot before,
            TimeRecordAuditSnapshot after,
            String correctionReason);
}
