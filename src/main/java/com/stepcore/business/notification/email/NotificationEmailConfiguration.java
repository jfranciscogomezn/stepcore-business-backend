package com.stepcore.business.notification.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;
import java.util.Properties;

@Configuration
@Slf4j
public class NotificationEmailConfiguration {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    JavaMailSender loggingJavaMailSender() {
        final JavaMailSenderImpl sender = new JavaMailSenderImpl() {
            @Override
            public void send(final org.springframework.mail.SimpleMailMessage simpleMessage) {
                log.info(
                        "[NotificationEmailSender] - STUB: to={} subject={} body={}",
                        simpleMessage.getTo() != null ? String.join(",", simpleMessage.getTo()) : "",
                        simpleMessage.getSubject(),
                        simpleMessage.getText());
            }

            @Override
            public void send(final org.springframework.mail.SimpleMailMessage... simpleMessages) {
                for (final org.springframework.mail.SimpleMailMessage message : simpleMessages) {
                    send(message);
                }
            }
        };
        sender.setHost("localhost");
        final Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        return sender;
    }
}
