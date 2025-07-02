package org.lanestel.application.exception.mqtt;

public class InvalidMqttIdException extends RuntimeException {
    public InvalidMqttIdException(String message) {
        super(message);
    }
}
