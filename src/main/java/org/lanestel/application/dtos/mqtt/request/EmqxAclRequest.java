package org.lanestel.application.dtos.mqtt.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for EMQX HTTP ACL (authorization) requests.
 * Contains client and topic information for authorization.
 */
@Data
public class EmqxAclRequest {
    @JsonProperty("clientid")
    private String clientId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("topic")
    private String topic;
    
    @JsonProperty("action")
    private String action; // "publish" or "subscribe"
    
    @JsonProperty("peerhost")
    private String peerHost;
    
    @JsonProperty("protocol")
    private String protocol;
    
    @JsonProperty("mountpoint")
    private String mountpoint;
}