package org.lanestel.domain.pojo.device;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DevicePOJO {
    private String deviceName;
    private String clientId;
    private String mqttId;
    private String mqttPassword;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
