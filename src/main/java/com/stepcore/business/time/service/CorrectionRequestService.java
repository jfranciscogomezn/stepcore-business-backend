package com.stepcore.business.time.service;

import com.stepcore.business.time.controller.dto.CorrectionRequestResponse;
import com.stepcore.business.time.controller.dto.CreateCorrectionRequestRequest;
import com.stepcore.business.time.controller.dto.DismissCorrectionRequestRequest;

import java.util.List;

public interface CorrectionRequestService {

    CorrectionRequestResponse submit(String actorEmail, Long timeRecordId, CreateCorrectionRequestRequest request);

    CorrectionRequestResponse dismiss(String actorEmail, Long requestId, DismissCorrectionRequestRequest request);

    List<CorrectionRequestResponse> listPending();

    void autoResolve(Long timeRecordId);

    List<CorrectionRequestResponse> listPendingForEmployee(String actorEmail);
}
