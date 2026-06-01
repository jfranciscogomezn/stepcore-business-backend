package com.stepcore.business.security;

/**
 * Menu-option permission codes mirrored from the security service JWT {@code permissions} claim.
 */
public final class AppPermissions {

    public static final String PAYROLL_CONFIG = "PAYROLL_CONFIG";
    public static final String EMPLOYEE_CONFIG = "EMPLOYEE_CONFIG";

    private AppPermissions() {
    }
}
