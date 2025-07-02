package org.lanestel.domain.entity.mqtt_account;

/**
 * Enum representing permission types for access control
 */
public enum Permission {
    /**
     * Grant access to the specified topic and action
     */
    ALLOW,
    
    /**
     * Deny access to the specified topic and action
     */
    DENY
}