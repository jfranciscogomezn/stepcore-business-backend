package com.stepcore.business.operations.controller;

import com.stepcore.business.operations.controller.dto.CreateTransportDocumentRequest;
import com.stepcore.business.operations.controller.dto.TransportDocumentResponse;
import com.stepcore.business.operations.service.OsiTransportDocumentService;
import com.stepcore.business.security.AppPermissions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operations/osi/{osiId}/vehicles/{vehicleId}/documents")
@RequiredArgsConstructor
public class OsiTransportDocumentController {

    private final OsiTransportDocumentService documentService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public List<TransportDocumentResponse> list(@PathVariable Long osiId, @PathVariable Long vehicleId) {
        return documentService.list(osiId, vehicleId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.OPS_OSI + "')")
    public ResponseEntity<TransportDocumentResponse> add(
            @PathVariable Long osiId,
            @PathVariable Long vehicleId,
            @Valid @RequestBody CreateTransportDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.add(osiId, vehicleId, request));
    }
}
