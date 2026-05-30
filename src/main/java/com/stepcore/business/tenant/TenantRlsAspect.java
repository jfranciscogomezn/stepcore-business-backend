package com.stepcore.business.tenant;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(10)
@RequiredArgsConstructor
public class TenantRlsAspect {

    private final TenantGuc tenantGuc;

    @Around("@within(org.springframework.transaction.annotation.Transactional) "
            + "|| @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object bindTenant(final ProceedingJoinPoint joinPoint) throws Throwable {
        tenantGuc.bind(TenantContext.getTenantIdOrDefault());
        return joinPoint.proceed();
    }
}
