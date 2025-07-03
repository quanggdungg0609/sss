package org.lanestel.infrastructures.exception.mqtt;

public class FindByClientIdException extends RuntimeException {

    public FindByClientIdException(String message, Throwable cause) {
        super(message, cause);
    }

}
