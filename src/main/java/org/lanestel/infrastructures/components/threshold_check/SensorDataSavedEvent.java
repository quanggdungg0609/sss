package org.lanestel.infrastructures.components.threshold_check;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SensorDataSavedEvent {
        public final Long deviceId;
        public final String clientId;
        public final Map<String, Object> dataMap;

        @JsonCreator
        public SensorDataSavedEvent(
            @JsonProperty("deviceId") Long deviceId, 
            @JsonProperty("clientId") String clientId, 
            @JsonProperty("dataMap") Map<String, Object> dataMap) {
            this.deviceId = deviceId;
            this.clientId = clientId;
            this.dataMap = dataMap;
        }
    }