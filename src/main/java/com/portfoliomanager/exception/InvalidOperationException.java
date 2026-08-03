package com.portfoliomanager.exception;

/** Thrown for business-rule violations, e.g. selling more units than currently held. */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
