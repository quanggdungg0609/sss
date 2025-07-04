package org.lanestel.infrastructures.entity.sensor_data_entity;

import java.time.LocalDateTime;
import java.util.Map;

// Thay thế import
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing sensor data received from devices via MQTT.
 * Each sensor data record is associated with a specific device and contains
 * the timestamp when data was sent and the actual sensor data in JSONB format.
 */
@Data
@Entity
@Builder
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(
    name = "sensor_data",
    indexes = {
        @Index(name = "idx_sensor_data_device_id", columnList = "device_id"),
        @Index(name = "idx_sensor_data_date", columnList = "date"),
        @Index(name = "idx_sensor_data_created_at", columnList = "created_at"),
        @Index(name = "idx_sensor_data_device_date", columnList = "device_id, date")
    }
)
public class SensorDataEntity extends PanacheEntity {

    /**
     * Many-to-one relationship with Device.
     * Each sensor data record belongs to exactly one device.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", referencedColumnName = "id", nullable = false)
    private DeviceEntity device;

    /**
     * Timestamp when the sensor data was sent from the camera/sensor.
     * This represents the actual time when the data was captured/sent,
     * not when it was received by the system.
     */
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    /**
     * Sensor data in JSONB format stored as Map for type-safe access.
     * This field can store any JSON structure containing sensor readings,
     * camera data, or other device-specific information.
     * The Map allows for easy access to nested JSON properties.
     */
   
    /**
     * Sensor data in JSONB format stored as Map for type-safe access.
     */
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> data;

    /**
     * Timestamp when this sensor data record was created in the system
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this sensor data record was last updated
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Automatically sets the creation timestamp before persisting the entity.
     * Called automatically by JPA before the entity is persisted to the database.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

   
    /**
     * Automatically updates the modification timestamp before updating the entity.
     * Called automatically by JPA before the entity is updated in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Sets the device for this sensor data record.
     * 
     * @param device The device that sent this sensor data
     */
    public void setDevice(DeviceEntity device) {
        this.device = device;
    }

    /**
     * Sets the date when the sensor data was captured/sent.
     * 
     * @param date The timestamp when data was sent from the device
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * Sets the sensor data as a Map for type-safe JSON handling.
     * 
     * @param data The sensor data as Map<String, Object>
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
