package com.stepcore.business.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class TenantGuc {

    static final String SETTING = "app.current_tenant";

    @PersistenceContext
    private EntityManager entityManager;

    public void bind(final Long tenantId) {
        if (tenantId == null || !TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }
        entityManager
                .createNativeQuery("SELECT set_config(:name, :value, true)")
                .setParameter("name", SETTING)
                .setParameter("value", String.valueOf(tenantId))
                .getSingleResult();
    }
}
