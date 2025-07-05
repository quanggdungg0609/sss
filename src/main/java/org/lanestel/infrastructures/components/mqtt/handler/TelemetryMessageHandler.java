package org.lanestel.infrastructures.components.mqtt.handler;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.Map;

import org.jboss.logging.Logger;
import org.lanestel.infrastructures.components.threshold_check.SensorDataSavedEvent;
import org.lanestel.infrastructures.components.threshold_check.ThresholdCheckComponent;
import org.lanestel.infrastructures.dao.device.DeviceDAO;
import org.lanestel.infrastructures.dao.sensor_data.SensorDataDAO;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
@ApplicationScoped
public class TelemetryMessageHandler extends IMqttMessageHandler {
    @Inject
    Logger log;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    DeviceDAO deviceDAO;

    @Inject 
    SensorDataDAO sensorDataDAO;

    @Inject
    Vertx vertx;


    @Override
    public String getHandledTopicType() {
        return "telemetry";
    }

    @Override
    @WithTransaction
    public Uni<Void> handle(MqttMessage<byte[]> message) {
        Map<String, Object> payloadMap;
        try {
            String data = new String(message.getPayload());
            log.info(data);
            payloadMap = objectMapper.readValue(data, new TypeReference<>() {});
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

       return Panache.withTransaction(() -> 
            deviceDAO.findByMqttClientId(clientId)
                .onItem().ifNotNull().transformToUni(deviceEntity -> {
                    log.info("Tx1: Found device " + deviceEntity.getDeviceName() + ". Saving sensor data.");
                    LocalDateTime eventDate = (timestampStr != null)
                                        ? ZonedDateTime.parse(timestampStr).toLocalDateTime()
                                        : LocalDateTime.now(ZoneId.of("UTC"));
                    
                    return sensorDataDAO.create(deviceEntity, eventDate, dataMap)
                        .map(savedEntity -> deviceEntity); // Pass the device entity forward
                })
        )
        // --- CHAINING: Publish to Event Bus AFTER Transaction 1 Succeeds ---
        .onItem().ifNotNull().invoke(deviceEntity -> {
            log.info("Publishing to event bus for device ID: " + deviceEntity.id);
            
            // Create a payload object
            SensorDataSavedEvent payloadObject = new SensorDataSavedEvent(
                deviceEntity.id,
                clientId,
                dataMap
            );

            // Publish the object to the event bus. Vert.x will use Jackson to serialize it.
            vertx.eventBus().publish(ThresholdCheckComponent.THRESHOLD_CHECK_ADDRESS, JsonObject.mapFrom(payloadObject));
        })
        .onFailure().invoke(e -> log.error("Transaction 1 (Save Data) failed for topic: " + message.getTopic(), e))
        .replaceWithVoid();
    }
}