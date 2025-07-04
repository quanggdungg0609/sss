package org.lanestel.infrastructures.dao.threshold;

import org.lanestel.infrastructures.entity.threshold_entity.ThresholdEntity;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ThresholdDAO implements PanacheRepository<ThresholdEntity>{
    public Uni<ThresholdEntity>findByDeviceNameAndSensorKey(String deviceName, String sensorKey){
        return find("deviceName = ?1 and sensorKey = ?2", deviceName, sensorKey).firstResult();
    }

}
