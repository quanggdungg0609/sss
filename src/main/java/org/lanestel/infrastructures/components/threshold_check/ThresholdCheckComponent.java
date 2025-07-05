package org.lanestel.infrastructures.components.threshold_check;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.lanestel.infrastructures.components.cache.ThresholdCache;
import org.lanestel.infrastructures.components.notification.NotificationComponent;
import org.lanestel.infrastructures.dao.threshold.ThresholdDAO;
import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;
import org.lanestel.infrastructures.entity.threshhold_cache_item.ThresholdCacheItem;
import org.lanestel.infrastructures.entity.threshold_entity.ThresholdEntity;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.vertx.core.json.JsonObject;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class ThresholdCheckComponent {
    public static final String THRESHOLD_CHECK_ADDRESS = "threshold-check";

    @Inject
    Logger log;

    @Inject
    Mutiny.SessionFactory sf;

    @Inject
    ThresholdDAO thresholdDAO;

    @Inject
    ThresholdCache thresholdCache;

    @Inject
    NotificationComponent notificationComponent;

    @ConsumeEvent(THRESHOLD_CHECK_ADDRESS)
    public void consumeThresholdCheckEvent(JsonObject payload) {
        SensorDataSavedEvent event = payload.mapTo(SensorDataSavedEvent.class);
        log.info("Received event from bus for client: " + event.clientId);

        Uni<Void> transactionUni = Panache.withTransaction(() ->
            handleThresholds(event)
        );

        transactionUni.subscribe().with(
            success -> log.info("Threshold processing transaction completed for client: " + event.clientId),
            failure -> log.error("Threshold processing transaction failed for client: " + event.clientId, failure)
        );
    }

    private Uni<Void> handleThresholds(SensorDataSavedEvent event) {
        log.infof("Processing sensor keys for deviceId: %d", event.deviceId);

        return Multi.createFrom().iterable(event.dataMap.keySet())
            .onItem().transformToUni(sensorKey ->
                findOrCreateThreshold(event.deviceId, sensorKey, event.clientId)
            )
            .concatenate()
            .collect().asList()
            .onItem().transformToUni(thresholdItems -> {
                // After collecting thresholds, check for violations
                return checkAllThresholds(thresholdItems, event.dataMap);
            })
            .onItem().transformToUni(violations -> {
                // **DEBUGGING CHANGE**: Integrate email sending into the main reactive chain.
                // A failure here will now cause the entire transactionUni to fail, making errors visible.
                if (violations != null && !violations.isEmpty()) {
                    return notificationComponent.sendViolationAlert(event.deviceId, violations);
                }
                // If there are no violations, return a completed Uni so the chain can continue.
                return Uni.createFrom().voidItem();
            });
    }

    private Uni<ThresholdCacheItem> findOrCreateThreshold(Long deviceId, String sensorKey, String clientId) {
        String cacheKey = thresholdCache.buildCacheKey(clientId, sensorKey);
        
        return thresholdCache.getThreshold(cacheKey, key -> {
            return ThresholdEntity.<ThresholdEntity>find("device.id = ?1 and sensorKey = ?2", deviceId, sensorKey)
                .firstResult()
                .onItem().transformToUni(foundThreshold -> {
                    if (foundThreshold != null) {
                        log.info("Found existing threshold for key: " + key);
                        return Uni.createFrom().item(ThresholdCacheItem.fromEntity(foundThreshold));
                    } else {
                        log.info("No threshold found for key: " + key + ". Starting creation process.");
                        return DeviceEntity.<DeviceEntity>findById(deviceId)
                            .onItem().ifNotNull().transformToUni(managedDevice -> {
                                log.infof("Creating new threshold for device '%s' and key '%s'.", managedDevice.getDeviceName(), sensorKey);
                                ThresholdEntity newThreshold = new ThresholdEntity();
                                newThreshold.setDevice(managedDevice);
                                newThreshold.setSensorKey(sensorKey);
                                newThreshold.setWarningMessage("Warning");

                                return newThreshold.persist()
                                    .onItem().transform(persisted -> ThresholdCacheItem.fromEntity(newThreshold));
                            })
                            .onItem().ifNull().failWith(() -> new IllegalStateException("Cannot create threshold because device " + deviceId + " does not exist."));
                    }
                });
        });
    }

    /**
     * Checks a list of thresholds against the received sensor data.
     * @param thresholds The list of threshold items to check.
     * @param dataMap The map of sensor data.
     * @return A Uni containing a list of all found violations.
     */
    private Uni<List<Violation>> checkAllThresholds(List<ThresholdCacheItem> thresholds, Map<String, Object> dataMap) {
        return Multi.createFrom().iterable(thresholds)
            .onItem().transformToUni(threshold -> checkSingleThreshold(threshold, dataMap))
            .concatenate()
            .collect().asList()
            .map(listsOfViolations -> 
                // Flatten the list of lists into a single list of violations
                listsOfViolations.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList())
            );
    }

    /**
     * Checks a single threshold against its corresponding value in the data map.
     * @param threshold The threshold item.
     * @param dataMap The map of sensor data.
     * @return A Uni containing a list of violations for this single check (can be 0, 1, or 2).
     */
    private Uni<List<Violation>> checkSingleThreshold(ThresholdCacheItem threshold, Map<String, Object> dataMap) {
        Object rawValue = dataMap.get(threshold.sensorKey());
        
        BigDecimal value;
        try {
            value = convertToBigDecimal(rawValue);
        } catch (Exception e) {
            log.errorf("Invalid value for sensor %s: %s. Skipping check.", threshold.sensorKey(), rawValue);
            return Uni.createFrom().item(new ArrayList<>()); // Return empty list on conversion error
        }

        List<Violation> violations = new ArrayList<>();

        // Check against minValue if it's not null
        if (threshold.minValue() != null && value.compareTo(threshold.minValue()) < 0) {
            String message = String.format("Value %.2f for '%s' is below min threshold %.2f", value, threshold.sensorKey(), threshold.minValue());
            violations.add(new Violation(threshold.sensorKey(), value, message));
            log.warn(message);
        }

        // Check against maxValue if it's not null
        if (threshold.maxValue() != null && value.compareTo(threshold.maxValue()) > 0) {
            String message = String.format("Value %.2f for '%s' is above max threshold %.2f", value, threshold.sensorKey(), threshold.maxValue());
            violations.add(new Violation(threshold.sensorKey(), value, message));
            log.warn(message);
        }

        return Uni.createFrom().item(violations);
    }

    /**
     * Handles the aggregated list of violations. For now, it just logs them.
     * @param deviceId The ID of the device that had violations.
     * @param violations The list of found violations.
     */
    private void handleViolations(Long deviceId, List<Violation> violations) {
        log.errorf("--- THRESHOLD VIOLATION ALERT for deviceId %d ---", deviceId);
        violations.forEach(v -> log.errorf(" > %s", v.message()));
        log.error("----------------------------------------------------");
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
