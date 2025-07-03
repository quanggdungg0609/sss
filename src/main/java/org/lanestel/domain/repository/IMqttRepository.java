package org.lanestel.domain.repository;

import io.smallrye.mutiny.Uni;
import org.lanestel.domain.entity.mqtt_account.MqttAccount;

public interface IMqttRepository {
    Uni<Boolean> checkMqttIdExist(String mqttId);
    Uni<Boolean> checkClientIdExists(String clientId);
    
    /**
     * Finds MQTT account by username (mqttId)
     * @param mqttId The MQTT username
     * @return Uni<MqttAccount> The MQTT account or null if not found
     */
    Uni<MqttAccount> findByMqttId(String mqttId);
    
    /**
     * Finds MQTT account by client ID
     * @param clientId The client ID
     * @return Uni<MqttAccount> The MQTT account or null if not found
     */
    Uni<MqttAccount> findByClientId(String clientId);
}
