package org.lanestel.application.service.device;

import org.lanestel.application.dtos.device.CreateDeviceRequest;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class DeviceService {


    public Uni<Response> createDevice(CreateDeviceRequest request){
        
        return Uni.createFrom().item(Response.ok().build());
    }

}
