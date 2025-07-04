package org.lanestel.infrastructures.dao.sensor_data;

import java.time.LocalDateTime;
import java.util.Map;

import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;
import org.lanestel.infrastructures.entity.sensor_data_entity.SensorDataEntity;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SensorDataDAO implements PanacheRepository<SensorDataEntity>{
    public Uni<SensorDataEntity> create(DeviceEntity device, LocalDateTime date,  Map<String, Object> data){
        SensorDataEntity entity = SensorDataEntity.builder()
            .device(device)
            .date(date)
            .data(data)
            .build();
        return entity.persist();
    }
}
