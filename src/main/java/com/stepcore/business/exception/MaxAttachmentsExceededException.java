package com.stepcore.business.exception;

public class MaxAttachmentsExceededException extends RuntimeException {
    public MaxAttachmentsExceededException(final int max) {
        super("This event type allows a maximum of " + max + " attachments");
    }
}
