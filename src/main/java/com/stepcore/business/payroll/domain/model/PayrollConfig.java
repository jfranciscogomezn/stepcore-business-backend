package com.stepcore.business.payroll.domain.model;

import com.stepcore.business.tenant.TenantContext;
import com.stepcore.business.tenant.TenantIdFilterResolver;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "payroll_configs")
@FilterDef(
        name = "tenantFilter",
        autoEnabled = true,
        parameters = @ParamDef(name = "tenantId", type = Long.class, resolver = TenantIdFilterResolver.class)
)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class PayrollConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private int year;

    @Column(name = "minimum_wage", nullable = false, precision = 14, scale = 2)
    private BigDecimal minimumWage;

    @Column(name = "transport_subsidy", nullable = false, precision = 14, scale = 2)
    private BigDecimal transportSubsidy;

    @Column(name = "monthly_work_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal monthlyWorkHours;

    @Column(name = "normal_daily_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal normalDailyHours;

    @Column(name = "max_daily_extra_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxDailyExtraHours;

    @Column(name = "daytime_start", nullable = false)
    private LocalTime daytimeStart;

    @Column(name = "daytime_end", nullable = false)
    private LocalTime daytimeEnd;

    @Column(name = "daytime_ot_start", nullable = false)
    private LocalTime daytimeOtStart;

    @Column(name = "daytime_ot_end", nullable = false)
    private LocalTime daytimeOtEnd;

    @Column(name = "night_surcharge_start", nullable = false)
    private LocalTime nightSurchargeStart;

    @Column(name = "night_surcharge_end", nullable = false)
    private LocalTime nightSurchargeEnd;

    @Column(name = "nocturnal_ot_start", nullable = false)
    private LocalTime nocturnalOtStart;

    @Column(name = "nocturnal_ot_end", nullable = false)
    private LocalTime nocturnalOtEnd;

    @Column(name = "sunday_ot_start", nullable = false)
    private LocalTime sundayOtStart;

    @Column(name = "sunday_ot_end", nullable = false)
    private LocalTime sundayOtEnd;

    @Column(name = "daytime_ot_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal daytimeOtFactor;

    @Column(name = "nocturnal_ot_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal nocturnalOtFactor;

    @Column(name = "night_surcharge_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal nightSurchargeFactor;

    @Column(name = "sunday_holiday_daytime_ot_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal sundayHolidayDaytimeOtFactor;

    @Column(name = "sunday_holiday_nocturnal_ot_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal sundayHolidayNocturnalOtFactor;

    @Column(name = "sunday_holiday_normal_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal sundayHolidayNormalFactor;

    @Column(name = "non_billable_rest_minutes", nullable = false)
    private int nonBillableRestMinutes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantIdOrDefault();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void apply(final PayrollConfig source) {
        this.minimumWage = source.minimumWage;
        this.transportSubsidy = source.transportSubsidy;
        this.monthlyWorkHours = source.monthlyWorkHours;
        this.normalDailyHours = source.normalDailyHours;
        this.maxDailyExtraHours = source.maxDailyExtraHours;
        this.daytimeStart = source.daytimeStart;
        this.daytimeEnd = source.daytimeEnd;
        this.daytimeOtStart = source.daytimeOtStart;
        this.daytimeOtEnd = source.daytimeOtEnd;
        this.nightSurchargeStart = source.nightSurchargeStart;
        this.nightSurchargeEnd = source.nightSurchargeEnd;
        this.nocturnalOtStart = source.nocturnalOtStart;
        this.nocturnalOtEnd = source.nocturnalOtEnd;
        this.sundayOtStart = source.sundayOtStart;
        this.sundayOtEnd = source.sundayOtEnd;
        this.daytimeOtFactor = source.daytimeOtFactor;
        this.nocturnalOtFactor = source.nocturnalOtFactor;
        this.nightSurchargeFactor = source.nightSurchargeFactor;
        this.sundayHolidayDaytimeOtFactor = source.sundayHolidayDaytimeOtFactor;
        this.sundayHolidayNocturnalOtFactor = source.sundayHolidayNocturnalOtFactor;
        this.sundayHolidayNormalFactor = source.sundayHolidayNormalFactor;
        this.nonBillableRestMinutes = source.nonBillableRestMinutes;
        this.updatedAt = LocalDateTime.now();
    }
}
