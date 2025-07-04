package org.lanestel.infrastructures.components.mqtt.handler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.lanestel.infrastructures.components.cache.ThresholdCache;
import org.lanestel.infrastructures.components.threshold_check.ThresholdCheckComponent.SensorDataSavedEvent;
import org.lanestel.infrastructures.dao.device.DeviceDAO;
import org.lanestel.infrastructures.dao.sensor_data.SensorDataDAO;
import org.lanestel.infrastructures.dao.threshold.ThresholdDAO;
import org.lanestel.infrastructures.entity.threshhold_cache_item.ThresholdCacheItem;
import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;
import org.lanestel.infrastructures.entity.threshold_entity.ThresholdEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class TelemetryMessageHandler extends IMqttMessageHandler {
    @Inject
    ObjectMapper objectMapper;

    @Inject
    ThresholdCache thresholdCache;

    @Inject
    Event<SensorDataSavedEvent> sensorDataSavedEvent;

    @Inject
    DeviceDAO deviceDAO;

    @Inject 
    SensorDataDAO sensorDataDAO;

    @Inject
    ThresholdDAO thresholdDAO;


    @Override
    public String getHandledTopicType() {
        return "telemetry";
    }

    @Override
    @WithTransaction
    public Uni<Void> handle(MqttMessage<byte[]> message) {
        Map<String, Object> payloadMap;
        try {
            payloadMap = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse telemetry payload for topic: " + message.getTopic(), e);
            return Uni.createFrom().failure(e);
        }

        String clientId = this.getClientId(message);
        Map<String, Object> dataMap = (Map<String, Object>) payloadMap.get("data");
        String timestampStr = (String) payloadMap.get("timestamp");

        if (dataMap == null || dataMap.isEmpty()) {
            log.warn("Empty or null data map for client: " + clientId);
            return Uni.createFrom().voidItem();
        }

        return deviceDAO.findByMqttClientId(clientId)
            .onItem().transformToUni(deviceEntity -> {
                if(deviceEntity != null){
                    log.info("Creating SensorDataEntity for device: " + deviceEntity.getDeviceName() + " with data: " + dataMap);
                    
                    LocalDateTime eventDate = (timestampStr != null)
                                        ? ZonedDateTime.parse(timestampStr).toLocalDateTime()
                                        : LocalDateTime.now(ZoneId.of("UTC"));
                    
                    return sensorDataDAO.create(deviceEntity, eventDate, dataMap)
                        .onItem().call(savedEntity -> {
                            log.info("Successfully saved SensorDataEntity for device: " + deviceEntity.getDeviceName());
                            sensorDataSavedEvent.fireAsync(new SensorDataSavedEvent(deviceEntity.id, clientId, dataMap));
                            return Uni.createFrom().voidItem();
                        })
                        .onFailure().invoke(throwable -> {
                            log.error("Failed to save SensorDataEntity for device: " + deviceEntity.id, throwable);
                        }).replaceWithVoid();
                }else{
                    log.warn("No device found for client: " + clientId);
                    return Uni.createFrom().voidItem();
                }
            }).replaceWithVoid();
    }
}