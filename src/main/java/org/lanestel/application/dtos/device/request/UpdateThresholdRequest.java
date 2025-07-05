package org.lanestel.application.dtos.device.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateThresholdRequest {
    @NotEmpty
    @JsonProperty("device_id")
    private Long deviceId;
    
    @NotBlank
    @JsonProperty("sensor_key")
    private String sensorKey;

    @NotEmpty
    @JsonProperty("new_min_value")
    private BigDecimal newMinValue;

    @NotEmpty
    @JsonProperty("new_max_value")
    private BigDecimal newMaxValue;
}
