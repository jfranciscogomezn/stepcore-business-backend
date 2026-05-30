package com.stepcore.business.tenant;

public final class TenantContext {

    public static final Long LEGACY_TENANT_ID = 2L;

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(final Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static Long getTenantIdOrDefault() {
        final Long tenantId = CURRENT_TENANT.get();
        return tenantId != null ? tenantId : LEGACY_TENANT_ID;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
