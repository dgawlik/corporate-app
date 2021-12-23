package org.dgawlik.exception;

public class NonExistingResourceException extends RuntimeException {

    public NonExistingResourceException(String msg) {
        super(msg);
    }
}
