package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.AddAttachmentRequest;
import com.stepcore.business.operations.controller.dto.AddCommentRequest;
import com.stepcore.business.operations.controller.dto.CreateCorrectiveEventRequest;
import com.stepcore.business.operations.controller.dto.CreateOsiEventRequest;
import com.stepcore.business.operations.controller.dto.OsiEventResponse;

import java.util.List;
import java.util.UUID;

public interface OsiEventService {

    OsiEventResponse create(Long osiId, Long vehicleId, CreateOsiEventRequest request,
                             Long authorUserId, UUID idempotencyKey);

    OsiEventResponse createCorrective(Long osiId, Long vehicleId, Long parentEventId,
                                       CreateCorrectiveEventRequest request, Long authorUserId);

    List<OsiEventResponse> list(Long osiId, Long vehicleId);

    OsiEventResponse approveVisibility(Long osiId, Long vehicleId, Long eventId, Long approverUserId);

    OsiEventResponse addAttachment(Long osiId, Long vehicleId, Long eventId, AddAttachmentRequest request);

    OsiEventResponse addComment(Long osiId, Long vehicleId, Long eventId,
                                 AddCommentRequest request, Long authorUserId);
}
