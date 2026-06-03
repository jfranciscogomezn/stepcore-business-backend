package com.stepcore.business.time.notification;

import com.stepcore.business.employee.domain.model.Employee;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.notification.email.NotificationEmailSender;
import com.stepcore.business.notification.recipient.AdminNotificationRecipientResolver;
import com.stepcore.business.notification.service.AdminNotificationService;
import com.stepcore.business.time.domain.model.TimeRecord;
import com.stepcore.business.time.domain.model.TimeRecordStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncompleteTimeRecordNotificationServiceTest {

    @Mock
    private AdminNotificationService adminNotificationService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AdminNotificationRecipientResolver recipientResolver;

    @Mock
    private NotificationEmailSender emailSender;

    @InjectMocks
    private IncompleteTimeRecordNotificationService notificationService;

    @Test
    void shouldPersistInAppNotificationAndSendEmailToAdmins() {
        final TimeRecord record = TimeRecord.builder()
                .withTenantId(2L)
                .withEmployeeId(10L)
                .withWorkDate(LocalDate.of(2026, 5, 29))
                .withClockIn(Instant.parse("2026-05-29T13:00:00Z"))
                .withStatus(TimeRecordStatus.INCOMPLETE)
                .build();
        final Employee employee = Employee.builder()
                .withId(10L)
                .withFirstName("Ana")
                .withLastName("Garcia")
                .build();

        when(employeeRepository.findAllById(List.of(10L))).thenReturn(List.of(employee));
        when(recipientResolver.resolveAdminEmails(2L)).thenReturn(List.of("admin@test.com"));

        notificationService.notifyAdminsOfIncompleteRecords(List.of(record));

        verify(adminNotificationService).saveIncompleteTimeRecordsNotification(
                anyString(),
                anyString(),
                anyList());
        verify(emailSender).send(anyString(), anyString(), eq(List.of("admin@test.com")));
    }
}
