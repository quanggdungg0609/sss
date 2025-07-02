package org.lanestel.application.dtos.device.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDeviceResponse {
    @JsonProperty("device_id")
    private String deviceName;
    @JsonProperty("mqtt_account")
    private String mqttAccount;
    @JsonProperty("mqtt_password")
    private String mqttPassword;
    @JsonProperty("client_id")
    private String clientId;
}
