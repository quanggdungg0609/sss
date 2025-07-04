package org.lanestel.infrastructures.dao.device;

import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class DeviceDAO implements PanacheRepository<DeviceEntity> {
    /**
     * Finds a single device by the MQTT client ID.
     * This method queries through the associated MqttAccountEntity.
     *
     * @param clientId The client ID of the MQTT account.
     * @return A Uni containing the found DeviceEntity, or null if no device is associated with the given client ID.
     */
    public Uni<DeviceEntity> findByMqttClientId(String clientId) {
        return find("mqttAccount.clientId", clientId).firstResult();
    }
}
