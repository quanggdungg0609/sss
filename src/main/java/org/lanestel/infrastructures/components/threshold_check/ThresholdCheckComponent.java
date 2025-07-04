package org.lanestel.infrastructures.components.threshold_check;

import java.util.Map;

import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.lanestel.infrastructures.components.cache.ThresholdCache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;


@ApplicationScoped
public class ThresholdCheckComponent {
    @Inject
    Logger log;

    @Inject
    Mutiny.SessionFactory sf;

    @Inject
    ThresholdCache thresholdCache;

    public void onSensorDataSaved(@ObservesAsync SensorDataSavedEvent event) {
        log.info("Received event");
    }

    public record SensorDataSavedEvent(
        Long deviceId, 
        String clientId, 
        Map<String, Object> dataMap
    ) {}
}
