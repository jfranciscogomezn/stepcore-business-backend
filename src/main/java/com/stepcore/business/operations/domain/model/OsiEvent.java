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
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "osi_events")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class OsiEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "osi_id", nullable = false)
    private Long osiId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "event_type_id", nullable = false)
    private Long eventTypeId;

    @Column(name = "author_user_id", nullable = false)
    private Long authorUserId;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(name = "captured_at_local")
    private OffsetDateTime capturedAtLocal;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "geo_lat", precision = 9, scale = 6)
    private BigDecimal geoLat;

    @Column(name = "geo_lng", precision = 9, scale = 6)
    private BigDecimal geoLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "effective_visibility", nullable = false, length = 30)
    private EventVisibility effectiveVisibility;

    @Column(name = "parent_event_id")
    private Long parentEventId;

    @Column(name = "correction_reason", columnDefinition = "TEXT")
    private String correctionReason;

    @Column(name = "idempotency_key", nullable = false)
    private UUID idempotencyKey;

    @Column(name = "external_party_name", length = 150)
    private String externalPartyName;

    @Column(name = "external_party_document", length = 50)
    private String externalPartyDocument;

    @PrePersist
    void onCreate() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantIdOrDefault();
        }
        if (this.receivedAt == null) {
            this.receivedAt = OffsetDateTime.now();
        }
    }
}
