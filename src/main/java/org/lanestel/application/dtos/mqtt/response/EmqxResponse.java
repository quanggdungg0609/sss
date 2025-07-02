package org.lanestel.application.dtos.mqtt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for EMQX HTTP authentication and authorization responses.
 * EMQX expects a simple JSON response with "result" field.
 */
@Data
@Builder
public class EmqxResponse {
    @JsonProperty("result")
    private String result; // "allow", "deny", or "ignore"
    
    /**
     * Creates an allow response
     */
    public static EmqxResponse allow() {
        return EmqxResponse.builder()
            .result("allow")
            .build();
    }
    
    /**
     * Creates a deny response
     */
    public static EmqxResponse deny() {
        return EmqxResponse.builder()
            .result("deny")
            .build();
    }
    
    /**
     * Creates an ignore response
     */
    public static EmqxResponse ignore() {
        return EmqxResponse.builder()
            .result("ignore")
            .build();
    }
}