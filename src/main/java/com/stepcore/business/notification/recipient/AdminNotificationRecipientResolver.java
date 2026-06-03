package com.stepcore.business.notification.recipient;

import java.util.List;

public interface AdminNotificationRecipientResolver {

    List<String> resolveAdminEmails(long tenantId);
}
