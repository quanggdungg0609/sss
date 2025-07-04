package org.lanestel.infrastructures.entity.threshhold_cache_item;

import java.math.BigDecimal;

import org.lanestel.infrastructures.entity.threshold_entity.ThresholdEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThresholdCacheItem {
    private String sensorKey;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String warningMessage; 


    public static ThresholdCacheItem fromEntity(ThresholdEntity entity) {
        return ThresholdCacheItem.builder()
            .sensorKey(entity.getSensorKey())
            .minValue(entity.getMinValue())
            .maxValue(entity.getMaxValue())
            .warningMessage(entity.getWarningMessage())
            .build();
    }
}
