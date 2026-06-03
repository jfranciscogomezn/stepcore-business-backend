package com.stepcore.business.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.notifications")
@Getter
@Setter
public class NotificationProperties {

    private String emailFrom = "noreply@stepcore.local";

    private boolean jdbcAdminLookupEnabled = true;

    private List<String> fallbackAdminEmails = new ArrayList<>();
}
