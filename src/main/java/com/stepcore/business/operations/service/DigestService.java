package com.stepcore.business.operations.service;

public interface DigestService {

    /**
     * Generates a plain-text digest of the client-visible state of an OSI.
     * Only events with effective_visibility = CLIENTE are included.
     * Internal notes and pending-approval events are never exposed.
     */
    String generate(Long osiId);
}
