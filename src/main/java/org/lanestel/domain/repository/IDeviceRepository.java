package org.lanestel.domain.repository;

import org.lanestel.domain.entity.device.Device;

import io.smallrye.mutiny.Uni;


public interface IDeviceRepository {
    Uni<Device> createDevice(Device device);
}
