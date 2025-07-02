package org.lanestel.domain.entity.mqtt_account;

import lombok.Builder;
import lombok.Data;
import org.lanestel.infrastructures.entity.mqtt_account_entity.MqttPermissionEntity;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Domain entity representing MQTT permissions for specific topics and actions.
 * This is a clean domain object without JPA annotations for business logic.
 */
@Data
@Builder
public class MqttPermission {
    
    /**
     * Unique identifier for the permission
     */
    private Long id;
    
    /**
     * ID of the MQTT account that owns this permission
     */
    private Long mqttAccountId;
    
    /**
     * Topic pattern for this permission (supports wildcards like +, #)
     * Examples: "sensors/+/temperature", "devices/#", "admin/commands"
     */
    private String topicPattern;
    
    /**
     * MQTT action allowed for this topic pattern
     */
    private MqttAction action;
    
    /**
     * Permission type (ALLOW or DENY)
     */
    private Permission permission;
    
    /**
     * List of allowed QoS levels for this permission
     * Default includes all QoS levels: 0 (At most once), 1 (At least once), 2 (Exactly once)
     */
    @Builder.Default
    private List<Integer> allowedQosLevels = Arrays.asList(0, 1, 2);
    
    /**
     * Timestamp when this permission was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this permission was last updated
     */
    private LocalDateTime updatedAt;
    
    /**
     * Creates a domain entity from JPA entity
     * @param entity The JPA entity to convert
     * @return The domain entity or null if input is null
     */
    public static MqttPermission fromEntity(MqttPermissionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return MqttPermission.builder()
            .id(entity.id)
            .mqttAccountId(entity.getMqttAccount() != null ? entity.getMqttAccount().id : null)
            .topicPattern(entity.getTopicPattern())
            .action(mapActionFromEntity(entity.getAction()))
            .permission(mapPermissionFromEntity(entity.getPermission()))
            .allowedQosLevels(entity.getAllowedQosLevels())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    /**
     * Converts this domain entity to JPA entity
     * @return The JPA entity
     */
    public MqttPermissionEntity toEntity() {
        MqttPermissionEntity entity = MqttPermissionEntity.builder()
            .topicPattern(this.topicPattern)
            .action(mapActionToEntity(this.action))
            .permission(mapPermissionToEntity(this.permission))
            .allowedQosLevels(this.allowedQosLevels)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
            
        if (this.id != null) {
            entity.id = this.id;
        }
        
        return entity;
    }
    
    /**
     * Updates an existing JPA entity with values from this domain object
     * @param entity The entity to update
     */
    public void updateEntity(MqttPermissionEntity entity) {
        if (entity == null) {
            return;
        }
        
        entity.setTopicPattern(this.topicPattern);
        entity.setAction(mapActionToEntity(this.action));
        entity.setPermission(mapPermissionToEntity(this.permission));
        entity.setAllowedQosLevels(this.allowedQosLevels);
        // Note: createdAt and updatedAt are managed by JPA lifecycle callbacks
    }
    
    /**
     * Maps JPA MqttAction enum to domain MqttAction enum
     * @param entityAction The JPA enum value
     * @return The domain enum value
     */
    private static MqttAction mapActionFromEntity(MqttPermissionEntity.MqttAction entityAction) {
        if (entityAction == null) {
            return null;
        }
        
        switch (entityAction) {
            case PUBLISH:
                return MqttAction.PUBLISH;
            case SUBSCRIBE:
                return MqttAction.SUBSCRIBE;
            case ALL:
                return MqttAction.ALL;
            default:
                throw new IllegalArgumentException("Unknown MqttAction: " + entityAction);
        }
    }
    
    /**
     * Maps domain MqttAction enum to JPA MqttAction enum
     * @param domainAction The domain enum value
     * @return The JPA enum value
     */
    private static MqttPermissionEntity.MqttAction mapActionToEntity(MqttAction domainAction) {
        if (domainAction == null) {
            return null;
        }
        
        switch (domainAction) {
            case PUBLISH:
                return MqttPermissionEntity.MqttAction.PUBLISH;
            case SUBSCRIBE:
                return MqttPermissionEntity.MqttAction.SUBSCRIBE;
            case ALL:
                return MqttPermissionEntity.MqttAction.ALL;
            default:
                throw new IllegalArgumentException("Unknown MqttAction: " + domainAction);
        }
    }
    
    /**
     * Maps JPA Permission enum to domain Permission enum
     * @param entityPermission The JPA enum value
     * @return The domain enum value
     */
    private static Permission mapPermissionFromEntity(MqttPermissionEntity.Permission entityPermission) {
        if (entityPermission == null) {
            return null;
        }
        
        switch (entityPermission) {
            case ALLOW:
                return Permission.ALLOW;
            case DENY:
                return Permission.DENY;
            default:
                throw new IllegalArgumentException("Unknown Permission: " + entityPermission);
        }
    }
    
    /**
     * Maps domain Permission enum to JPA Permission enum
     * @param domainPermission The domain enum value
     * @return The JPA enum value
     */
    private static MqttPermissionEntity.Permission mapPermissionToEntity(Permission domainPermission) {
        if (domainPermission == null) {
            return null;
        }
        
        switch (domainPermission) {
            case ALLOW:
                return MqttPermissionEntity.Permission.ALLOW;
            case DENY:
                return MqttPermissionEntity.Permission.DENY;
            default:
                throw new IllegalArgumentException("Unknown Permission: " + domainPermission);
        }
    }
    
    /**
     * Checks if this permission matches a given topic
     * Supports MQTT wildcards: + (single level) and # (multi level)
     * @param topic The topic to match against
     * @return true if the topic matches this permission's pattern
     */
    public boolean matchesTopic(String topic) {
        if (topicPattern == null || topic == null) {
            return false;
        }
        
        // Exact match
        if (topicPattern.equals(topic)) {
            return true;
        }
        
        // Convert MQTT wildcards to regex
        String regex = topicPattern
            .replace("+", "[^/]+")  // + matches single level
            .replace("#", ".*");     // # matches multiple levels
        
        return topic.matches(regex);
    }
    
    /**
     * Checks if the specified QoS level is allowed for this permission
     * @param qosLevel The QoS level to check (0, 1, or 2)
     * @return true if the QoS level is allowed, false otherwise
     */
    public boolean isQosLevelAllowed(int qosLevel) {
        return allowedQosLevels != null && allowedQosLevels.contains(qosLevel);
    }
    
    /**
     * MQTT action types
     */
    public enum MqttAction {
        PUBLISH,
        SUBSCRIBE,
        ALL
    }
    
    /**
     * Permission types
     */
    public enum Permission {
        ALLOW,
        DENY
    }
}