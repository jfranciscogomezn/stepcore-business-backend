package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.PortalOsiResponse;

import java.util.UUID;

public interface PublicPortalService {

    PortalOsiResponse getPortalData(UUID token, String remoteIp);
}
