package org.lanestel.infrastructures.entity.mqtt_account_entity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing MQTT permissions for specific topics and actions.
 * Each permission is associated with an MQTT account and defines access control rules.
 */
@Data
@Entity
@Builder
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "mqtt_permissions",
    indexes = {
        @Index(name = "idx_mqtt_permission_account_id", columnList = "mqtt_account_id"),
    }
)
@EqualsAndHashCode(callSuper=false)
public class MqttPermissionEntity extends PanacheEntity {
    
    /**
     * Reference to the MQTT account that owns this permission
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mqtt_account_id", nullable = false)
    private MqttAccountEntity mqttAccount;
    
    /**
     * Topic pattern for this permission (supports wildcards like +, #)
     * Examples: "sensors/+/temperature", "devices/#", "admin/commands"
     */
    @Column(name = "topic_pattern", nullable = false, length = 255)
    private String topicPattern;
    
    /**
     * MQTT action allowed for this topic pattern
     */
    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private MqttAction action;
    
    /**
     * Permission type (ALLOW or DENY)
     */
    @Column(name = "permission", nullable = false)
    @Enumerated(EnumType.STRING)
    private Permission permission;
    
    /**
     * List of allowed QoS levels for this permission
     * Default includes all QoS levels: 0 (At most once), 1 (At least once), 2 (Exactly once)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<Integer> allowedQosLevels = Arrays.asList(0, 1, 2);
    
    /**
     * Timestamp when this permission was created
     * Automatically set when entity is first persisted
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this permission was last updated
     * Automatically updated on every entity modification
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Automatically sets timestamps before persisting the entity to database.
     * Called only when creating a new permission (INSERT operation).
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        
        // Set default QoS levels if not already set
        if (this.allowedQosLevels == null || this.allowedQosLevels.isEmpty()) {
            this.allowedQosLevels = Arrays.asList(0, 1, 2);
        }
    }
    
    /**
     * Automatically updates the modification timestamp before updating the entity.
     * Called on every permission update (UPDATE operation).
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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
