package com.stepcore.business.exception;

public class VehicleRetiredException extends RuntimeException {
    public VehicleRetiredException(final Long vehicleId) {
        super("Vehicle " + vehicleId + " is RETIRED and cannot be assigned to an OSI");
    }
}
