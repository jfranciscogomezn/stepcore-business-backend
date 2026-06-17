package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.OsiEventAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OsiEventAttachmentRepository extends JpaRepository<OsiEventAttachment, Long> {

    List<OsiEventAttachment> findByEventIdOrderByCreatedAtAsc(Long eventId);

    long countByEventId(Long eventId);
}
