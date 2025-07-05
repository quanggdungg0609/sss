package org.lanestel.infrastructures.entity.threshhold_cache_item;

import java.math.BigDecimal;

import org.lanestel.infrastructures.entity.threshold_entity.ThresholdEntity;

import lombok.Builder;
import lombok.Data;

@Builder
public record ThresholdCacheItem(
    Long id,
    Long deviceId,
    String sensorKey,
    BigDecimal minValue,
    BigDecimal maxValue,
    String warningMessage
) {
    public static ThresholdCacheItem fromEntity(ThresholdEntity entity) {
        if (entity == null || entity.getDevice() == null) {
            return null;
        }
        return new ThresholdCacheItem(
            entity.id,
            entity.getDevice().id,
            entity.getSensorKey(),
            entity.getMinValue(),
            entity.getMaxValue(),
            entity.getWarningMessage()
        );
    }
}
