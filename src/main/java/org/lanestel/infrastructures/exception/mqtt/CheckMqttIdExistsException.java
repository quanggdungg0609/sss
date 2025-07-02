package org.lanestel.infrastructures.exception.mqtt;

public class CheckMqttIdExistsException extends RuntimeException {
    public CheckMqttIdExistsException(String message, Throwable cause){
        super(message, cause);
    }
    
}
