package org.lanestel.application.dtos.device.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDeviceRequest {
    @NotBlank(message = "Device name is required")
    @JsonProperty("device_name")
    private String deviceName;

    @NotBlank(message = "MQTT username is required")
    @JsonProperty("mqtt_username")
    private String mqttUsername;
}
