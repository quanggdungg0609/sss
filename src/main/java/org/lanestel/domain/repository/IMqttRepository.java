package org.lanestel.domain.repository;


import io.smallrye.mutiny.Uni;

public interface IMqttRepository {
    Uni<Boolean> checkMqttIdExist(String mqttId);
    Uni<Boolean> checkClientIdExists(String clientId);
}
