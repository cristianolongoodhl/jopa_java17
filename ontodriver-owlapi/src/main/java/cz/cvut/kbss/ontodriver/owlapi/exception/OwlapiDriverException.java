package cz.cvut.kbss.ontodriver.owlapi.exception;

import cz.cvut.kbss.ontodriver.exception.OntoDriverException;

/**
 * General exception for the OWLAPI driver.
 */
public class OwlapiDriverException extends OntoDriverException {

    public OwlapiDriverException() {
    }

    public OwlapiDriverException(String message) {
        super(message);
    }

    public OwlapiDriverException(Throwable cause) {
        super(cause);
    }

    public OwlapiDriverException(String message, Throwable cause) {
        super(message, cause);
    }
}
