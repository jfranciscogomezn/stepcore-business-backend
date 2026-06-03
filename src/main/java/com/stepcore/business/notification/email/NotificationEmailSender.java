package com.stepcore.business.notification.email;

import java.util.List;

public interface NotificationEmailSender {

    void send(String subject, String body, List<String> recipients);
}
