package org.lanestel.application.exception.mqtt;

public class MqttAccountAlreadyExistsException extends RuntimeException {
    public MqttAccountAlreadyExistsException(String message) {
        super(message);
    }
    
}
