package com.stepcore.business.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(final Long id) {
        super("Vehicle not found: " + id);
    }
}
