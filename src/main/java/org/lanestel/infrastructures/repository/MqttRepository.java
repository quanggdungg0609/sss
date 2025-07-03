package org.lanestel.infrastructures.repository;

import org.jboss.logging.Logger;
import org.lanestel.domain.entity.mqtt_account.MqttAccount;
import org.lanestel.domain.repository.IMqttRepository;
import org.lanestel.infrastructures.entity.mqtt_account_entity.MqttAccountEntity;
import org.lanestel.infrastructures.exception.mqtt.CheckMqttIdExistsException;
import org.lanestel.infrastructures.exception.mqtt.FindByClientIdException;
import org.lanestel.infrastructures.exception.mqtt.FindByMqttIdException;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MqttRepository implements IMqttRepository {
    private final Logger log; 

    @Inject
    public MqttRepository(Logger log) {
        this.log = log;
    }

    @Override
    public Uni<Boolean> checkMqttIdExist(String mqttId) {
        log.info("Checking Mqtt Id ....");
        return MqttAccountEntity.count("mqttId", mqttId)
            .onItem().transform(count -> count > 0)
            .onFailure().transform(throwable -> {
                log.error(throwable);
                throw new CheckMqttIdExistsException(throwable.getMessage(), throwable);
            });
    }

    @Override
    public Uni<Boolean> checkClientIdExists(String clientId) {
        log.info("Checking Client ID ....");
         return MqttAccountEntity.count("clientId", clientId)
            .onItem().transform(count -> count > 0)
            .onFailure().transform(throwable -> {
                log.error(throwable);
                throw new CheckMqttIdExistsException(throwable.getMessage(), throwable);
            });
    }
    
    /**
     * Finds MQTT account by mqttId with permissions eagerly loaded
     * @param mqttId The MQTT ID to search for
     * @return Uni containing the MqttAccount or null if not found
     */
    @Override
    public Uni<MqttAccount> findByMqttId(String mqttId) {
        log.info("Finding MQTT account by mqttId: " + mqttId);
        return MqttAccountEntity.find("SELECT m FROM MqttAccountEntity m LEFT JOIN FETCH m.permissions WHERE m.mqttId = ?1", mqttId)
            .firstResult()
            .onItem().transform(entity -> {
                if (entity == null) {
                    return null;
                }
                return MqttAccount.fromEntity((MqttAccountEntity) entity);
            })
            .onFailure().transform(throwable -> {
                log.error("Error finding MQTT account by mqttId", throwable);
                return new FindByMqttIdException("Failed to find MQTT account", throwable);
            });
    }
    
    /**
     * Finds MQTT account by clientId with permissions eagerly loaded
     * @param clientId The client ID to search for
     * @return Uni containing the MqttAccount or null if not found
     */
    @Override
    public Uni<MqttAccount> findByClientId(String clientId) {
        log.info("Finding MQTT account by clientId: " + clientId);
        return MqttAccountEntity.find("SELECT m FROM MqttAccountEntity m LEFT JOIN FETCH m.permissions WHERE m.clientId = ?1", clientId)
            .firstResult()
            .onItem().transform(entity -> {
                if (entity == null) {
                    return null;
                }
                return MqttAccount.fromEntity((MqttAccountEntity) entity);
            })
            .onFailure().transform(throwable -> {
                log.error("Error finding MQTT account by clientId", throwable);
                return new FindByClientIdException("Failed to find MQTT account", throwable);
            });
    }
}
