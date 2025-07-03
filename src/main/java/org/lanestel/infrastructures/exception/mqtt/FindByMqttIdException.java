package org.lanestel.infrastructures.exception.mqtt;

public class FindByMqttIdException extends RuntimeException{
    public FindByMqttIdException(String message, Throwable cause) {
        super(message, cause);
    }

}
