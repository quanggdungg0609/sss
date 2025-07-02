package org.lanestel.domain.usecase.device;

import org.lanestel.domain.pojo.device.DevicePOJO;

import io.smallrye.mutiny.Uni;

public interface ICreateDeviceUseCase {
    Uni<DevicePOJO> execute(String deviceName, String mqttUsername);
}
