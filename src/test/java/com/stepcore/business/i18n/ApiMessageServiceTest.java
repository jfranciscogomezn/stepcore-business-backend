package com.stepcore.business.i18n;

import com.stepcore.business.exception.IncompleteReportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ApiMessageServiceTest {

    private ApiMessageService apiMessageService;

    @BeforeEach
    void setUp() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        apiMessageService = new ApiMessageService(messageSource);
    }

    @Test
    void shouldLocalizeIncompleteReportInSpanish() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es-CO"));
        assertThat(apiMessageService.resolve(new IncompleteReportException(List.of()), null))
                .contains("incompletos");
    }

    @Test
    void shouldLocalizeTimeOperationInEnglish() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en-US"));
        assertThat(apiMessageService.resolveKey("error.time.alreadyClockedIn"))
                .isEqualTo("Employee already clocked in for today");
    }
}
