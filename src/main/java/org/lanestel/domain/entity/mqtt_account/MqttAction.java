package org.lanestel.domain.entity.mqtt_account;

/**
 * Enum representing MQTT action types for permission control
 */
public enum MqttAction {
    /**
     * Permission to publish messages to topics
     */
    PUBLISH,
    
    /**
     * Permission to subscribe to topics
     */
    SUBSCRIBE,
    
    /**
     * Permission for both publish and subscribe actions
     */
    ALL
}