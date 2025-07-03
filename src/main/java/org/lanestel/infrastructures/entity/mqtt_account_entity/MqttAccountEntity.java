package org.lanestel.infrastructures.entity.mqtt_account_entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
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
 * Entity representing MQTT account credentials and associated permissions.
 * Each account can have multiple permissions for different topics.
 */
@Data
@Entity
@Builder
@Cacheable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Table(
    name = "mqtt_accounts",
    indexes = {
        @Index(name = "idx_mqtt_account_mqtt_id", columnList = "mqtt_id"),
        @Index(name = "idx_mqtt_account_client_id", columnList = "client_id"),
    }
)
public class MqttAccountEntity extends PanacheEntity {
    
    /**
     * Unique MQTT identifier for authentication
     */
    @Column(name = "mqtt_id", unique = true, nullable = false, length = 100)
    private String mqttId;
    
    /**
     * Encrypted password for MQTT authentication
     */
    @Column(name = "mqtt_password", nullable = false, length = 255)
    private String mqttPassword;
    
    /**
     * MQTT client identifier
     */
    @Column(name = "client_id", length = 100, unique = true)
    private String clientId;
    
    /**
     * Timestamp when this MQTT account was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this MQTT account was last updated
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * List of permissions associated with this MQTT account.
     * Uses LAZY loading for performance optimization.
     */
    @OneToMany(
        mappedBy = "mqttAccount", 
        cascade = CascadeType.ALL, 
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<MqttPermissionEntity> permissions = new ArrayList<>();
    
    /**
     * Automatically sets the creation timestamp before persisting the entity
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    /**
     * Automatically updates the modification timestamp before updating the entity
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Adds a permission to this MQTT account
     * @param permission The permission to add
     */
    public void addPermission(MqttPermissionEntity permission) {
        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        permissions.add(permission);
        permission.setMqttAccount(this);
    }
    
    /**
     * Removes a permission from this MQTT account
     * @param permission The permission to remove
     */
    public void removePermission(MqttPermissionEntity permission) {
        permissions.remove(permission);
        permission.setMqttAccount(null);
    }
}
