package com.algorithms.exceptions;

public class DuplicateKeyException extends RuntimeException {

    public DuplicateKeyException() {
        super("Key already exists in the B-tree.");
    }
    public DuplicateKeyException(String message) {
        super(message);
    }
    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }
    public DuplicateKeyException(Throwable cause) {
        super(cause);
    }
}