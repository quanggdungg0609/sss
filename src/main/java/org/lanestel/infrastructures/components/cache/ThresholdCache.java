package org.lanestel.infrastructures.components.cache;


import java.time.Duration;
import java.util.function.Function;

import org.lanestel.infrastructures.entity.threshhold_cache_item.ThresholdCacheItem;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ThresholdCache {
    // An asynchronous, non-blocking cache
    private final AsyncCache<String, ThresholdCacheItem> cache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(30)) // Cache entries expire after 10 minutes
        .maximumSize(1000) // Maximum of 1000 entries
        .buildAsync();

    /**
     * Gets an item from the cache. If the item is not present, the mappingFunction
     * is called to load it from the database, and the result is stored in the cache.
     * @param key The cache key.
     * @param mappingFunction The function to execute on a cache miss to load the data.
     * @return A Uni containing the cached or newly loaded item.
     */
    public Uni<ThresholdCacheItem> getThreshold(String key, Function<String, Uni<ThresholdCacheItem>> mappingFunction) {
        // Uni.createFrom().completionStage(...) is the bridge between CompletableFuture (from Caffeine) and Uni (from Mutiny)
        return Uni.createFrom().completionStage(() -> cache.get(key, (k, executor) -> mappingFunction.apply(k).subscribeAsCompletionStage()));
    }

    /**
     * Helper method to build a consistent cache key.
     * @param clientId The client's unique identifier.
     * @param sensorKey The sensor key (e.g., "temp").
     * @return A formatted cache key string.
     */
    public String buildCacheKey(String clientId, String sensorKey) {
        return clientId + ":" + sensorKey;
    }
}
