/**
 * @author HTI students, Spring 2013
 *
 */
package org.thermostatapp.util;

public class InvalidInputValueException extends Exception {

    public InvalidInputValueException() {
        super();
    }

    public InvalidInputValueException(String msg) {
        super(msg);
    }

    public InvalidInputValueException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidInputValueException(Throwable cause) {
        super(cause);
    }
}