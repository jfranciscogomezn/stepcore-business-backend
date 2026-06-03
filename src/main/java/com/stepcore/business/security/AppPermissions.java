package com.stepcore.business.security;

/**
 * Menu-option permission codes mirrored from the security service JWT {@code permissions} claim.
 */
public final class AppPermissions {

    public static final String PAYROLL_CONFIG = "PAYROLL_CONFIG";
    public static final String EMPLOYEE_CONFIG = "EMPLOYEE_CONFIG";
    public static final String TIME_RECORDS_ADMIN = "TIME_RECORDS_ADMIN";
    public static final String MY_TIME = "MY_TIME";
    public static final String REPORTS = "REPORTS";

    private AppPermissions() {
    }
}
