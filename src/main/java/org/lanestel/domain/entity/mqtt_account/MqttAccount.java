package org.lanestel.domain.entity.mqtt_account;

import lombok.Builder;
import lombok.Data;

import org.lanestel.infrastructures.entity.mqtt_account_entity.MqttAccountEntity;
import org.lanestel.infrastructures.entity.mqtt_account_entity.MqttPermissionEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Domain entity representing MQTT account credentials and associated permissions.
 * This is a clean domain object without JPA annotations for business logic.
 */
@Data
@Builder
public class MqttAccount {
    
    /**
     * Unique identifier for the MQTT account
     */
    private Long id;
    
    /**
     * Unique MQTT identifier for authentication
     */
    private String mqttId;
    
    /**
     * Encrypted password for MQTT authentication
     */
    private String mqttPassword;
    
    /**
     * MQTT client identifier
     */
    private String clientId;
    
    /**
     * Timestamp when this MQTT account was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this MQTT account was last updated
     */
    private LocalDateTime updatedAt;
    
    /**
     * List of permissions associated with this MQTT account
     */
    private List<MqttPermission> permissions = new ArrayList<>();
    
    /**
     * Creates a domain entity from JPA entity
     * @param entity The JPA entity to convert
     * @return The domain entity or null if input is null
     */
    public static MqttAccount fromEntity(MqttAccountEntity entity) {
        if (entity == null) {
            return null;
        }
        
        List<MqttPermission> permissions = null;
        if (entity.getPermissions() != null) {
            permissions = entity.getPermissions().stream()
                .map(MqttPermission::fromEntity)
                .collect(Collectors.toList());
        }
        
        return MqttAccount.builder()
            .id(entity.id)
            .mqttId(entity.getMqttId())
            .mqttPassword(entity.getMqttPassword())
            .clientId(entity.getClientId())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .permissions(permissions)
            .build();
    }

    
    /**
     * Converts this domain entity to JPA entity
     * @return The JPA entity
     */
    public MqttAccountEntity toEntity() {
        List<MqttPermissionEntity> permissionEntities = null;
        if (this.permissions != null) {
            permissionEntities = this.permissions.stream()
                .map(MqttPermission::toEntity)
                .collect(Collectors.toList());
        }
        
        MqttAccountEntity entity = MqttAccountEntity.builder()
            .mqttId(this.mqttId)
            .mqttPassword(this.mqttPassword)
            .clientId(this.clientId)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .permissions(permissionEntities)
            .build();
            
        if (this.id != null) {
            entity.id = this.id;
        }
        
        // Set bidirectional relationship
        if (permissionEntities != null) {
            permissionEntities.forEach(p -> p.setMqttAccount(entity));
        }
        
        return entity;
    }
    
    /**
     * Updates an existing JPA entity with values from this domain object
     * @param entity The entity to update
     */
    public void updateEntity(MqttAccountEntity entity) {
        if (entity == null) {
            return;
        }
        
        entity.setMqttId(this.mqttId);
        entity.setMqttPassword(this.mqttPassword);
        entity.setClientId(this.clientId);
        // Note: createdAt and updatedAt are managed by JPA lifecycle callbacks
    }
    
    /**
     * Adds a permission to this MQTT account
     * @param permission The permission to add
     */
    public void addPermission(MqttPermission permission) {
        if (this.permissions == null) {
            this.permissions = new ArrayList<>();
        }
        this.permissions.add(permission);
    }
    
    /**
     * Removes a permission from this MQTT account
     * @param permission The permission to remove
     */
    public void removePermission(MqttPermission permission) {
        if (this.permissions != null) {
            this.permissions.remove(permission);
        }
    }
    
    /**
     * Checks if this account has a specific permission for a topic and action
     * @param topic The MQTT topic to check
     * @param action The MQTT action to check
     * @return true if permission is granted, false otherwise
     */
    public boolean hasPermission(String topic, MqttAction action) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        
        return permissions.stream()
            .filter(p -> p.getPermission() == Permission.ALLOW)
            .anyMatch(p -> p.matchesTopic(topic) && 
                     (p.getAction() == action || p.getAction() == MqttAction.ALL));
    }
    
}
