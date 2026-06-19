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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "osi_vehicle_assignments")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class OsiVehicleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "osi_id", nullable = false)
    private Long osiId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OsiVehicleState state;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assigned_user_ids", columnDefinition = "jsonb")
    @Builder.Default
    private List<Long> assignedUserIds = new ArrayList<>();

    @Column(name = "gps_provider", length = 100)
    private String gpsProvider;

    @Column(name = "gps_reference_url", length = 500)
    private String gpsReferenceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "hc_validation_status", nullable = false, length = 20)
    private HcValidationStatus hcValidationStatus;

    @Column(name = "hc_validation_notes", columnDefinition = "TEXT")
    private String hcValidationNotes;

    @Column(name = "hc_validated_by_user_id")
    private Long hcValidatedByUserId;

    @Column(name = "hc_validated_at")
    private OffsetDateTime hcValidatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantIdOrDefault();
        }
        if (this.state == null) {
            this.state = OsiVehicleState.PLANNED;
        }
        if (this.hcValidationStatus == null) {
            this.hcValidationStatus = HcValidationStatus.PENDIENTE;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
