package org.lanestel.infrastructures.components.threshold_check;

import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.lanestel.infrastructures.components.cache.ThresholdCache;
import org.lanestel.infrastructures.dao.threshold.ThresholdDAO;
import org.lanestel.infrastructures.entity.device_entity.DeviceEntity;
import org.lanestel.infrastructures.entity.threshhold_cache_item.ThresholdCacheItem;
import org.lanestel.infrastructures.entity.threshold_entity.ThresholdEntity;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.vertx.core.Vertx;
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
        return DeviceEntity.<DeviceEntity>findById(event.deviceId)
            .onItem().transformToUni(device -> {
                if (device == null) {
                    log.warnf("Device with id %d not found. Cannot process thresholds.", event.deviceId);
                    return Uni.createFrom().voidItem();
                }

                log.infof("Device '%s' found. Processing sensor keys.", device.getDeviceName());

                return Multi.createFrom().iterable(event.dataMap.keySet())
                    .onItem().transformToUni(sensorKey ->
                        // The logic now calls a method that uses the cache
                        findOrCreateThreshold(event.deviceId, sensorKey, event.clientId)
                    )
                    .concatenate()
                    .collect().asList()
                    .onItem().invoke(thresholdItems -> {
                        log.info("--- Found/Created Thresholds ---");
                        if (thresholdItems.isEmpty()) {
                            log.info("No thresholds were processed.");
                        } else {
                            thresholdItems.forEach(t ->
                                log.infof(" > DeviceID: %d, Key: %s, ID: %d, Warning: '%s'",
                                    t.deviceId(), t.sensorKey(), t.id(), t.warningMessage())
                            );
                        }
                        log.info("---------------------------------");
                    })
                    .replaceWithVoid();
            });
    }

    private Uni<ThresholdCacheItem> findOrCreateThreshold(Long deviceId, String sensorKey, String clientId) {
        String cacheKey = thresholdCache.buildCacheKey(clientId, sensorKey);
        
        return thresholdCache.getThreshold(cacheKey, key -> {
            log.info("Cache miss for key: " + key + ". Fetching from DB.");
            // **THE FIX**: Use the primitive deviceId for the query to avoid state issues.
            return ThresholdEntity.<ThresholdEntity>find("device.id = ?1 and sensorKey = ?2", deviceId, sensorKey)
                .firstResult()
                .onItem().transformToUni(foundThreshold -> {
                    // Case 1: Threshold was found. Convert to cache item and we are done.
                    if (foundThreshold != null) {
                        log.info("Found existing threshold for key: " + key);
                        return Uni.createFrom().item(ThresholdCacheItem.fromEntity(foundThreshold));
                    } else {
                        // Case 2: Threshold not found. We need to create it.
                        log.info("No threshold found for key: " + key + ". Starting creation process.");
                        // Fetch the device to associate with the new threshold.
                        return DeviceEntity.<DeviceEntity>findById(deviceId)
                            .onItem().ifNotNull().transformToUni(managedDevice -> {
                                // Create and persist the new threshold.
                                log.infof("Creating new threshold for device '%s' and key '%s'.", managedDevice.getDeviceName(), sensorKey);
                                ThresholdEntity newThreshold = new ThresholdEntity();
                                newThreshold.setDevice(managedDevice);
                                newThreshold.setSensorKey(sensorKey);
                                newThreshold.setWarningMessage("Warning");

                                return newThreshold.persist()
                                    // After persisting, transform the new entity into a cache item.
                                    .onItem().transform(persisted -> ThresholdCacheItem.fromEntity(newThreshold));
                            })
                            // If the device itself doesn't exist, we cannot create a threshold.
                            .onItem().ifNull().failWith(() -> new IllegalStateException("Cannot create threshold because device " + deviceId + " does not exist."));
                    }
                });
        });
    }
}
