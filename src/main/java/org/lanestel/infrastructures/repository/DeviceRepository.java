package org.lanestel.infrastructures.repository;

import org.lanestel.domain.entity.device.Device;
import org.lanestel.domain.repository.IDeviceRepository;
import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;
import org.lanestel.infrastructures.exception.device.CreateDeviceException;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeviceRepository implements IDeviceRepository {

    /**
     * Creates a new device in the database.
     * 
     * This method converts the domain Device entity to DeviceEntity,
     * persists it to the database using Panache reactive operations,
     * and returns the created device as a domain entity.
     *
     * @param device the domain device entity to be created
     * @return Uni<Device> a reactive stream containing the created device
     * @throws CreateDeviceException if there's an error during device creation
     */
    @Override
    public Uni<Device> createDevice(Device device) {
        try {
            // Convert domain entity to infrastructure entity
            DeviceEntity deviceEntity = device.toEntity();
            
            // Persist the entity using Panache reactive operations
            return deviceEntity.persist()
                .onItem().transform(persistedEntity -> {
                    // Convert back to domain entity and return
                    return Device.fromEntity((DeviceEntity) persistedEntity);
                })
                .onFailure().transform(throwable -> {
                    // Wrap any persistence exceptions in domain-specific exception
                    return new CreateDeviceException(
                        "Failed to create device: " + device.getDeviceName(), 
                        throwable
                    );
                });
        } catch (Exception e) {
            // Handle any immediate conversion errors
            return Uni.createFrom().failure(
                new CreateDeviceException(
                    "Failed to prepare device for creation: " + device.getDeviceName(), 
                    e
                )
            );
        }
    }
}
