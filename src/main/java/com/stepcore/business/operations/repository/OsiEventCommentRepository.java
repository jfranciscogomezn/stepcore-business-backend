package com.stepcore.business.operations.repository;

import com.stepcore.business.operations.domain.model.OsiEventComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OsiEventCommentRepository extends JpaRepository<OsiEventComment, Long> {

    List<OsiEventComment> findByEventIdOrderByCreatedAtAsc(Long eventId);
}
