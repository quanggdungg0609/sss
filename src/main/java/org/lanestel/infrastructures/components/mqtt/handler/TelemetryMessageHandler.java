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
import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;
import org.lanestel.infrastructures.entity.sensor_data_entity.SensorDataEntity;
import org.lanestel.infrastructures.entity.threshold_entity.ThresholdEntity;

import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.hibernate.reactive.mutiny.Mutiny;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;



@ApplicationScoped
public class TelemetryMessageHandler extends IMqttMessageHandler {
    @Inject
    ObjectMapper objectMapper;

    @Inject
    ThresholdCache thresholdCache;

    @Inject
    Mutiny.SessionFactory sf;

    @Override
    public String getHandledTopicType() {
        return "telemetry"; 
    }

    @Override
    public Uni<Void> handle(MqttMessage<byte[]> message) {
        return sf.withSession(session -> {
            log.info("Executing Telemetry logic for topic: " + message.getTopic());
            String clientId = this.getClientId(message);
            try {
                Map<String, Object> payloadMap = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});
                Map<String, Object> dataMap = (Map<String, Object>) payloadMap.get("data");
                String timestampStr = (String) payloadMap.get("timestamp");
        
                if (dataMap == null || dataMap.isEmpty()) {
                    log.warn("Empty or null data map for client: " + clientId);
                    return Uni.createFrom().voidItem();
                }
        
                return session.withTransaction(tx -> 
                    DeviceEntity.<DeviceEntity>find("mqttAccount.clientId", clientId).firstResult()
                        .onItem().ifNotNull().<Void>transformToUni(device -> {
                            log.info("Found device: " + device.getDeviceName() + " for client: " + clientId);
                            return checkAllThresholdsAndAutoCreate(device, clientId, dataMap).chain(
                                violations -> {
                                    if (!violations.isEmpty()) {
                                        sendNotifications(device, violations);
                                    }
                                    
                                    LocalDateTime eventDate = (timestampStr != null)
                                        ? ZonedDateTime.parse(timestampStr).toLocalDateTime()
                                        : LocalDateTime.now(ZoneId.of("UTC"));
                                    
                                    log.info("Creating SensorDataEntity for device: " + device.id + " with data: " + dataMap);
                                    
                                    SensorDataEntity sensorData = SensorDataEntity.builder()
                                        .device(device)
                                        .date(eventDate)
                                        .data(dataMap)
                                        .build();
                                    
                                    return sensorData.persist()
                                        .onItem().invoke(savedEntity -> {
                                            log.info("Successfully saved SensorDataEntity for device: " + device.id);
                                        })
                                        .onFailure().invoke(throwable -> {
                                            log.error("Failed to save SensorDataEntity for device: " + device.id, throwable);
                                        })
                                        .replaceWithVoid();
                                }
                            );
                        })
                        .onItem().ifNull().switchTo(() -> {
                            log.warn("No device found for client: " + clientId);
                            return Uni.createFrom().voidItem();
                        })
                );
            } catch (Exception e) {
                log.error("Failed to parse telemetry payload for client: " + clientId, e);
                return Uni.createFrom().voidItem();
            }
        });
    }

   private Uni<List<Violation>> checkAllThresholdsAndAutoCreate(DeviceEntity device, String clientId, Map<String, Object> dataMap) {
       return Multi.createFrom().iterable(dataMap.keySet())
            .onItem().transformToUni(sensorKey -> {
                String cacheKey = thresholdCache.buildCacheKey(clientId, sensorKey);
                return thresholdCache.getThreshold(cacheKey, key -> {
                        log.info("Cache miss for key: " + key);
                        return ThresholdEntity
                            .<ThresholdEntity>find("SELECT t FROM ThresholdEntity t WHERE t.device = ?1 AND t.sensorKey = ?2", device, sensorKey)
                            .firstResult()
                            .onItem().ifNull().switchTo(() -> createDefaultThreshold(device, sensorKey));
                })
                .chain(threshold -> checkSingleThreshold(dataMap, threshold));
            })
            .concatenate()
            .collect().asList()
            .map(violationLists -> violationLists.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    private Uni<List<Violation>> checkSingleThreshold(Map<String, Object> dataMap, ThresholdEntity threshold) {
        Object rawValue = dataMap.get(threshold.getSensorKey());
        String sensorKey = threshold.getSensorKey();
        BigDecimal value;
        try {
            value = convertToBigDecimal(rawValue);
        } catch (Exception e) {
            log.errorf("Invalid value for sensor %s: %s", sensorKey, rawValue);
            return Uni.createFrom().item(new ArrayList<>());
        }
        List<Violation> violations = new ArrayList<>();
                if (threshold.getMinValue() != null && value.compareTo(threshold.getMinValue()) < 0) {
                    // violated min threshold
                    String message = threshold.getWarningMessage() != null ? 
                        threshold.getWarningMessage() : 
                        String.format("Value %.2f is below min threshold %.2f", value, threshold.getMinValue());
                    
                    violations.add(new Violation(sensorKey, value, message));
                }
                if (threshold.getMaxValue() != null && value.compareTo(threshold.getMaxValue()) > 0) {
                    // violated max threshold
                    String message = threshold.getWarningMessage() != null ? 
                        threshold.getWarningMessage() : 
                        String.format("Value %.2f is above max threshold %.2f", value, threshold.getMaxValue());
                    
                    violations.add(new Violation(sensorKey, value, message));
                }
        return Uni.createFrom().item(violations);
    }

    private Uni<ThresholdEntity> createDefaultThreshold(DeviceEntity device, String sensorKey) {
        log.infof("Auto-creating default threshold for device %d and sensor key '%s'", device.id, sensorKey);
        ThresholdEntity newThreshold = ThresholdEntity.builder()
                .device(device).sensorKey(sensorKey)
                .warningMessage(String.format("Warning for %s", sensorKey)).build();
        return newThreshold.persist();
    }

    private void sendNotifications(DeviceEntity device, List<Violation> violations) {
        log.errorf("--- THRESHOLD VIOLATION ALERT for device %s (%s) ---", device.getDeviceName(), device.id);
        violations.forEach(v -> log.errorf(" > %s", v.message()));
        log.error("---------------------------------------------------------");
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        
        throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
    }

    public record Violation(String sensorKey, BigDecimal violatedValue, String message) {}
}
