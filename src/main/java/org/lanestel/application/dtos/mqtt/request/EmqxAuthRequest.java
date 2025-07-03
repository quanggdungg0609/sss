package org.lanestel.application.dtos.mqtt.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for EMQX HTTP authentication requests.
 * Contains client connection information for authentication.
 */
@Data
public class EmqxAuthRequest {
    @JsonProperty("clientid")
    private String clientId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("peerhost")
    private String peerHost;
    
    @JsonProperty("mountpoint")
    private String mountpoint;
}