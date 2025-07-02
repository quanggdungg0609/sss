package org.lanestel.infrastructures.repository;

import org.jboss.logging.Logger;
import org.lanestel.domain.repository.IMqttRepository;
import org.lanestel.infrastructures.entity.mqtt_account_entity.MqttAccountEntity;
import org.lanestel.infrastructures.exception.mqtt.CheckMqttIdExistsException;

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

}
