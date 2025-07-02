package org.lanestel.infrastructures.entity.mqtt_account_entity;

import java.time.LocalDateTime;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
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
     * Priority for permission evaluation (higher number = higher priority)
     * Used when multiple rules match the same topic
     */
    @Column(name = "priority")
    private Integer priority;
    
    /**
     * Optional description for this permission rule
     */
    @Column(name = "description", length = 500)
    private String description;
    
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
