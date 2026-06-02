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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_records")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class TimeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "clock_in", nullable = false)
    private Instant clockIn;

    @Column(name = "clock_out")
    private Instant clockOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TimeRecordStatus status;

    @Column(nullable = false)
    @Builder.Default
    private boolean corrected = false;

    @Column(name = "original_clock_in")
    private Instant originalClockIn;

    @Column(name = "original_clock_out")
    private Instant originalClockOut;

    @Column(name = "correction_reason", length = 500)
    private String correctionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantIdOrDefault();
        }
        final LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void registerClockOut(final Instant clockOutTime) {
        this.clockOut = clockOutTime;
        this.status = TimeRecordStatus.CLOSED;
    }

    public boolean isOpen() {
        return status == TimeRecordStatus.OPEN;
    }
}
