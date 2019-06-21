package com.jaravir.tekila.ui.jsf.exception.handler;

/**
 * @author ElmarMa on 10/3/2018
 */
public class UserBlockedException extends RuntimeException {

    public UserBlockedException() {
    }

    public UserBlockedException(String message) {
        super(message);
    }

    public UserBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
