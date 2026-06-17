package com.stepcore.business.notification.domain.model;

import com.stepcore.business.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;

@Entity
@Table(name = "employee_notifications")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeNotification {

    public static final String TYPE_CORRECTION_REQUEST_SUBMITTED = "TIME_CORRECTION_REQUEST_SUBMITTED";
    public static final String TYPE_TIME_RECORD_REOPENED         = "TIME_RECORD_REOPENED";
    public static final String TYPE_TIME_RECORD_CORRECTED        = "TIME_RECORD_CORRECTED";
    public static final String TYPE_TIME_RECORD_CREATED_BY_ADMIN = "TIME_RECORD_CREATED_BY_ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "notification_type", nullable = false, length = 60)
    private String notificationType;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Setter
    @Column(nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantIdOrDefault();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
