package com.stepcore.business.time.domain.model;

import com.stepcore.business.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;

import java.time.Instant;

@Entity
@Table(name = "time_correction_requests")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class TimeCorrectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "time_record_id", nullable = false)
    private Long timeRecordId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(nullable = false, columnDefinition = "text")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TimeCorrectionRequestStatus status;

    @Column(name = "resolution_note", columnDefinition = "text")
    private String resolutionNote;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @PrePersist
    void onCreate() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantIdOrDefault();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public void resolve() {
        this.status = TimeCorrectionRequestStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }

    public void dismiss(final Long adminUserId, final String reason) {
        this.status = TimeCorrectionRequestStatus.DISMISSED;
        this.resolvedBy = adminUserId;
        this.resolutionNote = reason;
        this.resolvedAt = Instant.now();
    }

    public boolean isPending() {
        return this.status == TimeCorrectionRequestStatus.PENDING;
    }
}
