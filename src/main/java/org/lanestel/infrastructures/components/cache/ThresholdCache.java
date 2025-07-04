package org.lanestel.infrastructures.components.cache;


import java.util.function.Function;

import org.lanestel.infrastructures.entity.threshhold_cache_item.ThresholdCacheItem;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ThresholdCache {
    private final Cache cache;

    @Inject
    public ThresholdCache(
        @CacheName("device-thresholds") Cache cache
    ) {
        this.cache = cache;
    }

    public Uni<ThresholdCacheItem> getThreshold(String cacheKey, Function<String, Uni<ThresholdCacheItem>> fallback) {
        Uni<Uni<ThresholdCacheItem>> nestedUni = cache.get(cacheKey, fallback);
        return nestedUni.chain(innerUni -> innerUni);
    }


    public Uni<Void> invalidateThreshold(String clientId, String sensorKey) {
        return cache.invalidate(buildCacheKey(clientId, sensorKey));
    }

    public String buildCacheKey(String clientId, String sensorKey) {
        return clientId + "::" + sensorKey;
    }
}
