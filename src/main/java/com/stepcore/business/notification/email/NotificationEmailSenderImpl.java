package com.stepcore.business.notification.email;

import com.stepcore.business.notification.config.NotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEmailSenderImpl implements NotificationEmailSender {

    private final NotificationProperties notificationProperties;
    private final JavaMailSender javaMailSender;

    @Override
    public void send(final String subject, final String body, final List<String> recipients) {
        if (recipients.isEmpty()) {
            log.warn("[NotificationEmailSender] - SKIP: no recipients for subject={}", subject);
            return;
        }

        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(notificationProperties.getEmailFrom());
        message.setTo(recipients.toArray(String[]::new));
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
        log.info("[NotificationEmailSender] - SENT: subject={} recipients={}", subject, recipients.size());
    }
}
