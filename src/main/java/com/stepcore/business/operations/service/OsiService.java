package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.ChangeOsiOwnerRequest;
import com.stepcore.business.operations.controller.dto.CreateOsiRequest;
import com.stepcore.business.operations.controller.dto.OsiResponse;
import com.stepcore.business.operations.controller.dto.OsiSummaryResponse;
import com.stepcore.business.operations.controller.dto.UpdateOsiRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OsiService {

    OsiResponse create(CreateOsiRequest request, Long coordinatorUserId);

    Page<OsiSummaryResponse> list(String status, String dateFrom, String dateTo, Pageable pageable);

    OsiResponse findById(Long id);

    OsiResponse update(Long id, UpdateOsiRequest request);

    OsiResponse changeOwner(Long id, ChangeOsiOwnerRequest request);
}
