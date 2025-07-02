package org.lanestel.application.controller.device;

import org.jboss.logging.Logger;
import org.lanestel.application.dtos.device.CreateDeviceRequest;
import org.lanestel.application.service.device.DeviceService;

import jakarta.validation.constraints.NotNull;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceController {
    private final Logger log;
    private final DeviceService deviceService;

    @Inject
    public DeviceController(
        Logger log,
        DeviceService deviceService
    ){
        this.log = log;
        this.deviceService = deviceService;
    }
    
    /**
     * Creates a new device with MQTT account.
     * 
     * This endpoint handles device creation requests by delegating to the DeviceService.
     * The service will create both the device and its associated MQTT account.
     *
     * @param request the device creation request containing device name and MQTT username
     * @return Uni<Response> a reactive HTTP response containing the created device data or error
     */
    @POST
    @Path("/create")
    @WithTransaction 
    public Uni<Response> createDevice(@NotNull(message = "Request body is required") @Valid CreateDeviceRequest request){
        log.info("=== Device Creation Request START ===");
        log.info("Device Name: " + request.getDeviceName());
        log.info("MQTT Username: " + request.getMqttUsername());
        
        return deviceService.createDevice(request)
            .onItem().invoke(response -> {
                log.info("Device creation completed with status: " + response.getStatus());
                log.info("=== Device Creation Request END ===");
            })
            .onFailure().invoke(throwable -> {
                log.error("Device creation failed: " + throwable.getMessage(), throwable);
                log.info("=== Device Creation Request END (FAILED) ===");
            });
    }
}
