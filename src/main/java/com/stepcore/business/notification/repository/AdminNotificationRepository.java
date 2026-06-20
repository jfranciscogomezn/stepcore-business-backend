package com.stepcore.business.notification.repository;

import com.stepcore.business.notification.domain.model.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    List<AdminNotification> findTop20ByOrderByCreatedAtDesc();

    List<AdminNotification> findTop20ByNotificationTypeInOrderByCreatedAtDesc(Collection<String> types);
}
