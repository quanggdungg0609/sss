package org.lanestel.infrastructures.repository;

import org.lanestel.domain.entity.device.Device;
import org.lanestel.domain.repository.IDeviceRepository;
import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeviceRepository implements IDeviceRepository {

    @Override
    public Device createDevice(Device device) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }
}
