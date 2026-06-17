package com.stepcore.business.notification.repository;

import com.stepcore.business.notification.domain.model.EmployeeNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeNotificationRepository extends JpaRepository<EmployeeNotification, Long> {

    List<EmployeeNotification> findTop20ByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);
}
