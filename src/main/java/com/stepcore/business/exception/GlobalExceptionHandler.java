package com.stepcore.business.exception;

import com.stepcore.business.i18n.ApiMessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ApiMessageService apiMessageService;

    public record ErrorResponse(String timestamp, int status, String error, String message, String path) {}

    public record IncompleteReportErrorResponse(
            String timestamp,
            int status,
            String error,
            String message,
            String path,
            java.util.List<java.time.LocalDate> incompleteDates) {}

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            final AuthenticationException ex, final HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, apiMessageService.resolve(ex, ex.getMessage()), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            final AccessDeniedException ex, final HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, apiMessageService.resolve(ex, ex.getMessage()), request);
    }

    @ExceptionHandler({PayrollConfigNotFoundException.class, HolidayNotFoundException.class,
                        EmployeeNotFoundException.class, TimeRecordNotFoundException.class,
                        EmployeeProfileNotLinkedException.class, CorrectionRequestNotFoundException.class,
                        ClientNotFoundException.class, VehicleNotFoundException.class,
                        OsiNotFoundException.class, OsiEventNotFoundException.class,
                        EventTypeNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(
            final RuntimeException ex, final HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, apiMessageService.resolve(ex, ex.getMessage()), request);
    }

    @ExceptionHandler({DuplicateHolidayException.class, DuplicateEmployeeDocumentException.class,
                        DuplicateEmployeeEmailException.class, DuplicateTimeRecordException.class,
                        DuplicateCorrectionRequestException.class, CorrectionRequestNotPendingException.class,
                        DuplicateClientNameException.class, DuplicateVehiclePlateException.class})
    public ResponseEntity<ErrorResponse> handleConflict(
            final RuntimeException ex, final HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, apiMessageService.resolve(ex, ex.getMessage()), request);
    }

    @ExceptionHandler({VehicleRetiredException.class, OsiAlreadyClosedException.class,
                        InvalidStateTransitionException.class, MaxAttachmentsExceededException.class,
                        ChecklistViolationException.class, HcValidationPendingException.class})
    public ResponseEntity<ErrorResponse> handleUnprocessableEntity(
            final RuntimeException ex, final HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, apiMessageService.resolve(ex, ex.getMessage()), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            final MethodArgumentNotValidException ex, final HttpServletRequest request) {
        final String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                apiMessageService.resolveKey("error.validationFailed", details),
                request);
    }

    @ExceptionHandler(InvalidTimeRecordOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeRecordOperation(
            final InvalidTimeRecordOperationException ex, final HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, apiMessageService.resolve(ex, ex.getMessage()), request);
    }

    @ExceptionHandler({InvalidReportPeriodException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(
            final RuntimeException ex, final HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, apiMessageService.resolve(ex, ex.getMessage()), request);
    }

    @ExceptionHandler(IncompleteReportException.class)
    public ResponseEntity<IncompleteReportErrorResponse> handleIncompleteReport(
            final IncompleteReportException ex, final HttpServletRequest request) {
        final IncompleteReportErrorResponse body = new IncompleteReportErrorResponse(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                apiMessageService.resolve(ex, ex.getMessage()),
                request.getRequestURI(),
                ex.getIncompleteDates());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            final NoResourceFoundException ex, final HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                apiMessageService.resolve(ex, "API endpoint not found. Ensure the business backend includes the required module."),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            final Exception ex, final HttpServletRequest request) {
        log.error("[GlobalExceptionHandler] - UNEXPECTED: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, apiMessageService.resolveKey("error.unexpected"), request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            final HttpStatus status, final String message, final HttpServletRequest request) {
        final ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
