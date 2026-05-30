package com.stepcore.business.tenant;

import java.util.function.Supplier;

public class TenantIdFilterResolver implements Supplier<Long> {

    @Override
    public Long get() {
        return TenantContext.getTenantIdOrDefault();
    }
}
