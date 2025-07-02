package org.lanestel.application.usecase.mqtt;

import org.lanestel.domain.repository.IMqttRepository;
import org.lanestel.domain.usecase.mqtt.ICheckMqttIdExistsUseCase;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CheckMqttIdExistsUseCase implements ICheckMqttIdExistsUseCase{
    private final IMqttRepository mqttRepository;

    @Inject
    public CheckMqttIdExistsUseCase(IMqttRepository mqttRepository) {
        this.mqttRepository = mqttRepository;
    }

    @Override
    public Uni<Boolean> checkExists(String mqttId) {
        return mqttRepository.checkMqttIdExist(mqttId);
    }
}
