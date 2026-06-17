package com.stepcore.business.exception;

public class DuplicateVehiclePlateException extends RuntimeException {
    public DuplicateVehiclePlateException(final String plate) {
        super("A vehicle with plate '" + plate + "' already exists for this tenant");
    }
}
