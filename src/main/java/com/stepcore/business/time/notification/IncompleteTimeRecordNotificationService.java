package com.stepcore.business.time.notification;

import com.stepcore.business.employee.domain.model.Employee;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.notification.email.NotificationEmailSender;
import com.stepcore.business.notification.model.IncompleteRecordNotificationItem;
import com.stepcore.business.notification.recipient.AdminNotificationRecipientResolver;
import com.stepcore.business.notification.service.AdminNotificationService;
import com.stepcore.business.time.domain.model.TimeRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncompleteTimeRecordNotificationService {

    private final AdminNotificationService adminNotificationService;
    private final EmployeeRepository employeeRepository;
    private final AdminNotificationRecipientResolver recipientResolver;
    private final NotificationEmailSender emailSender;

    public void notifyAdminsOfIncompleteRecords(final List<TimeRecord> records) {
        if (records.isEmpty()) {
            return;
        }

        final List<IncompleteRecordNotificationItem> items = buildItems(records);
        final String title = "Incomplete time records require attention";
        final String message = buildMessage(items);

        adminNotificationService.saveIncompleteTimeRecordsNotification(title, message, items);

        final long tenantId = records.get(0).getTenantId();
        final List<String> recipients = recipientResolver.resolveAdminEmails(tenantId);
        emailSender.send(title, message, recipients);

        log.info(
                "[IncompleteTimeRecordNotificationService] - NOTIFIED: tenantId={} records={} recipients={}",
                tenantId,
                records.size(),
                recipients.size());
    }

    private List<IncompleteRecordNotificationItem> buildItems(final List<TimeRecord> records) {
        final Map<Long, Employee> employeesById = employeeRepository.findAllById(
                        records.stream().map(TimeRecord::getEmployeeId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Employee::getId, employee -> employee));

        return records.stream()
                .map(record -> {
                    final Employee employee = employeesById.get(record.getEmployeeId());
                    final String employeeName = employee != null
                            ? employee.getFirstName() + " " + employee.getLastName()
                            : "Employee #" + record.getEmployeeId();
                    return new IncompleteRecordNotificationItem(
                            record.getEmployeeId(),
                            employeeName,
                            record.getWorkDate());
                })
                .toList();
    }

    private String buildMessage(final List<IncompleteRecordNotificationItem> items) {
        final String details = items.stream()
                .map(item -> "- " + item.employeeName() + " on " + item.workDate())
                .collect(Collectors.joining(System.lineSeparator()));
        return items.size() + " time record(s) were marked INCOMPLETE because employees did not clock out."
                + System.lineSeparator()
                + System.lineSeparator()
                + details;
    }
}
