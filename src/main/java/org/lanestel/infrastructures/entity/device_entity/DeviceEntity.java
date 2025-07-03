package org.lanestel.infrastructures.entity.device_entity;

import java.time.LocalDateTime;

import org.lanestel.infrastructures.entity.mqtt_account_entity.MqttAccountEntity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Index;

/**
 * Entity representing a device with its associated MQTT account.
 * Each device has a one-to-one relationship with an MQTT account.
 */
@Getter
@Setter
@Entity
@Builder
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "devices",
    indexes = {
        @Index(name = "idx_device_device_name", columnList = "device_name"),
        @Index(name = "idx_device_mqtt_account_id", columnList = "mqtt_account_id"),
        @Index(name = "idx_device_status", columnList = "status"),
        @Index(name = "idx_device_created_at", columnList = "created_at")
    }
)
public class DeviceEntity extends PanacheEntity {

    /**
     * Human-readable name of the device
     */
    @Column(name = "device_name", nullable = false, length = 255)
    private String deviceName;

    /**
     * One-to-one relationship with MQTT account.
     * Each device has exactly one MQTT account for authentication.
     */
    @OneToOne(
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @JoinColumn(name = "mqtt_account_id", referencedColumnName = "id")
    private MqttAccountEntity mqttAccount;

    /**
     * Timestamp when this device was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this device was last updated
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Current status of the device
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    /**
     * Automatically sets the creation timestamp before persisting the entity
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = DeviceStatus.ACTIVE;
        }
    }

    /**
     * Automatically updates the modification timestamp before updating the entity
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static enum DeviceStatus {
        ACTIVE,
        INACTIVE,
    }
}


