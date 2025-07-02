package org.lanestel.domain.usecase.mqtt;

import io.smallrye.mutiny.Uni;

public interface ICheckMqttIdExistsUseCase {
    Uni<Boolean> checkExists(String mqttId);
}
