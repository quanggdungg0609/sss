package org.lanestel.domain.repository;

import org.lanestel.domain.entity.mqtt_account.MqttAccount;

import io.smallrye.mutiny.Uni;

public interface IMqttRepository {
    Uni<Boolean> checkMqttIdExist(String mqttId);
}
