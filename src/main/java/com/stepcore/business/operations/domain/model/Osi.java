package com.stepcore.business.operations.domain.model;

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
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "osi")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class Osi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "osi_number", nullable = false, length = 30)
    private String osiNumber;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(nullable = false, length = 300)
    private String origin;

    @Column(nullable = false, length = 300)
    private String destination;

    @Column(name = "load_window_start")
    private OffsetDateTime loadWindowStart;

    @Column(name = "load_window_end")
    private OffsetDateTime loadWindowEnd;

    @Column(name = "delivery_window_start")
    private OffsetDateTime deliveryWindowStart;

    @Column(name = "delivery_window_end")
    private OffsetDateTime deliveryWindowEnd;

    @Column(name = "commercial_reference", length = 150)
    private String commercialReference;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OsiStatus status;

    @Column(name = "coordinator_user_id")
    private Long coordinatorUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @PrePersist
    void onCreate() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantIdOrDefault();
        }
        if (this.status == null) {
            this.status = OsiStatus.DRAFT;
        }
    }
}
