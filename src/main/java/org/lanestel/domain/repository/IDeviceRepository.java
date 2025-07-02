package org.lanestel.domain.repository;

import org.lanestel.domain.entity.device.Device;


public interface IDeviceRepository {
    Device createDevice(Device device);
}
