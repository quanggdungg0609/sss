package org.lanestel.domain.entity.device;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import org.lanestel.domain.entity.mqtt_account.MqttAccount;
import org.lanestel.domain.pojo.device.DevicePOJO;
import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;

/**
 * Domain entity representing a device with its associated MQTT account.
 * This is a clean domain object without JPA annotations for business logic.
 */
@Data
@Builder
public class Device {
    
    /**
     * Unique identifier for the device
     */
    private Long id;
    
    /**
     * Human-readable name of the device
     */
    private String deviceName;
    
    /**
     * Associated MQTT account for this device
     */
    private MqttAccount mqttAccount;
    
    /**
     * Timestamp when this device was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this device was last updated
     */
    private LocalDateTime updatedAt;
    
    /**
     * Current status of the device
     */
    private Status status = Status.ACTIVE;

    /**
     * Device status enumeration
     */
    public enum Status {
        ACTIVE,
        INACTIVE
    }
    
    /**
     * Creates a domain entity from JPA entity
     * @param entity The JPA entity to convert
     * @return The domain entity or null if input is null
     */
    public static Device fromEntity(DeviceEntity entity) {
        if (entity == null) {
            return null;
        }
        
        MqttAccount mqttAccount = null;
        if (entity.getMqttAccount() != null) {
            mqttAccount = MqttAccount.fromEntity(entity.getMqttAccount());
        }
        
        Status domainStatus = null;
        if (entity.getStatus() != null) {
            domainStatus = Status.valueOf(entity.getStatus().name());
        }
        
        return Device.builder()
            .id(entity.id)
            .deviceName(entity.getDeviceName())
            .mqttAccount(mqttAccount)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .status(domainStatus)
            .build();
    }
    
    /**
     * Converts this domain entity to JPA entity
     * @return The JPA entity
     */
    public DeviceEntity toEntity() {
        DeviceEntity.DeviceStatus entityStatus = null;
        if (this.status != null) {
            entityStatus = DeviceEntity.DeviceStatus.valueOf(this.status.name());
        }
        
        DeviceEntity entity = DeviceEntity.builder()
            .deviceName(this.deviceName)
            .mqttAccount(this.mqttAccount != null ? this.mqttAccount.toEntity() : null)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .status(entityStatus)
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
    public void updateEntity(DeviceEntity entity) {
        if (entity == null) {
            return;
        }
        
        entity.setDeviceName(this.deviceName);
        
        if (this.mqttAccount != null) {
            if (entity.getMqttAccount() != null) {
                this.mqttAccount.updateEntity(entity.getMqttAccount());
            } else {
                entity.setMqttAccount(this.mqttAccount.toEntity());
            }
        }
        
        if (this.status != null) {
            entity.setStatus(DeviceEntity.DeviceStatus.valueOf(this.status.name()));
        }
        
        // Note: createdAt and updatedAt are managed by JPA lifecycle callbacks
    }

    public DevicePOJO toPojo() {
        return DevicePOJO.builder()
            .deviceName(this.deviceName)
            .clientId(this.mqttAccount.getClientId())
            .mqttId(this.mqttAccount.getMqttId())
            .mqttPassword(this.mqttAccount.getMqttPassword())
            .status(this.status.name())
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}
