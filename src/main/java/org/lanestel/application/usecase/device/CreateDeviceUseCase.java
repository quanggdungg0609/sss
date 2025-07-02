package org.lanestel.application.usecase.device;

import org.lanestel.application.exception.mqtt.MqttAccountAlreadyExistsException;
import org.lanestel.common.utils.password_util.IPasswordUtil;
import org.lanestel.common.utils.uid_util.IUidUtil;
import org.lanestel.domain.entity.device.Device;
import org.lanestel.domain.entity.mqtt_account.MqttAccount;
import org.lanestel.domain.entity.mqtt_account.MqttPermission;
import org.lanestel.domain.entity.mqtt_account.MqttPermission.MqttAction;
import org.lanestel.domain.entity.mqtt_account.MqttPermission.Permission;
import org.lanestel.domain.pojo.device.DevicePOJO;
import org.lanestel.domain.repository.IDeviceRepository;
import org.lanestel.domain.repository.IMqttRepository;
import org.lanestel.domain.usecase.device.ICreateDeviceUseCase;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Use case implementation for creating new devices with MQTT accounts.
 * This class handles the business logic for device creation including
 * MQTT username validation, password generation, and device persistence.
 */
@ApplicationScoped
public class CreateDeviceUseCase implements ICreateDeviceUseCase {
    private final IPasswordUtil passwordUtil;
    private final IUidUtil uidUtil;
    private final IDeviceRepository deviceRepository;
    private final IMqttRepository mqttRepository;

    /**
     * Constructor for CreateDeviceUseCase with dependency injection.
     *
     * @param passwordUtil utility for generating secure passwords
     * @param uidUtil utility for generating unique identifiers
     * @param deviceRepository repository for device data operations
     * @param mqttRepository repository for MQTT account operations
     */
    @Inject
    public CreateDeviceUseCase(
        IPasswordUtil passwordUtil,
        IUidUtil uidUtil, 
        IDeviceRepository deviceRepository, 
        IMqttRepository mqttRepository
    ) {
        this.passwordUtil = passwordUtil;
        this.uidUtil = uidUtil;
        this.deviceRepository = deviceRepository;
        this.mqttRepository = mqttRepository;
    }

    /**
     * Executes the device creation process.
     * 
     * This method performs the following operations:
     * 1. Validates that the MQTT username is not already in use
     * 2. Generates a unique device UID and secure password with retry mechanism
     * 3. Creates MQTT account with hashed password and default permissions
     * 4. Creates device entity
     * 5. Persists the device to the repository
     * 6. Returns a DevicePOJO with the raw password for initial setup
     *
     * @param deviceName the name of the device to be created
     * @param mqttUsername the MQTT username for the device's MQTT account
     * @return Uni<DevicePOJO> a reactive stream containing the created device data with raw password
     * @throws MqttAccountAlreadyExistsException if the MQTT username already exists in the system
     */
    @Override
    public Uni<DevicePOJO> execute(String deviceName, String mqttUsername) {
       return mqttRepository.checkMqttIdExist(mqttUsername).onItem().transformToUni(mqttUsernameExists -> {
            if(mqttUsernameExists) {
                return Uni.createFrom().failure(new MqttAccountAlreadyExistsException("Mqtt username already exists: " + mqttUsername));
            }
            
            // Generate unique clientId with retry mechanism
            return generateUniqueClientId(0).onItem().transformToUni(deviceUid -> {
                String rawPassword = passwordUtil.generatePassword(8);
                String hashedPassword = passwordUtil.hash(rawPassword);
        
                // Create default MQTT permissions for the device
                List<MqttPermission> defaultPermissions = createDefaultPermissions(deviceUid);
        
                MqttAccount mqttAccount = MqttAccount.builder()
                        .mqttId(mqttUsername)
                        .mqttPassword(hashedPassword) // Use hashed password
                        .clientId(deviceUid)
                        .permissions(defaultPermissions)
                        .build();
                        
                Device newDevice = Device.builder()
                        .deviceName(deviceName)
                        .mqttAccount(mqttAccount)
                        .build();
                        
                // Create device and return POJO with raw password
                return deviceRepository.createDevice(newDevice).onItem().transform(device ->{
                    DevicePOJO devicePOJO = device.toPojo();
                    // Set raw password instead of hashed password for response
                    devicePOJO.setMqttPassword(rawPassword);
                    return devicePOJO;
                });
            });
       });
    }
    
    /**
     * Generates a unique client ID with retry mechanism.
     * 
     * This method attempts to generate a unique client ID up to 5 times.
     * If all attempts fail, it throws an exception.
     *
     * @param attempt the current attempt number (0-based)
     * @return Uni<String> a reactive stream containing the unique client ID
     * @throws RuntimeException if unable to generate unique client ID after 5 attempts
     */
    private Uni<String> generateUniqueClientId(int attempt) {
        if (attempt >= 5) {
            return Uni.createFrom().failure(new RuntimeException("Unable to generate unique client ID after 5 attempts"));
        }
        
        String deviceUid = uidUtil.generateUid();
        
        return mqttRepository.checkClientIdExists(deviceUid).onItem().transformToUni(exists -> {
            if (exists) {
                // Client ID already exists, retry with next attempt
                return generateUniqueClientId(attempt + 1);
            } else {
                // Client ID is unique, return it
                return Uni.createFrom().item(deviceUid);
            }
        });
    }
    
    /**
     * Creates default MQTT permissions for a new device.
     * 
     * Default permissions include:
     * - sensor/[clientId]/telemetry (PUBLISH) - for sending sensor data with all QoS levels
     * - sensor/[clientId]/status (PUBLISH) - for LWT (Last Will Testament) messages with all QoS levels
     * - sensor/[clientId]/command (SUBSCRIBE) - for receiving commands from server with all QoS levels
     * 
     * All permissions allow QoS levels 0, 1, and 2 by default.
     *
     * @param clientId the unique client ID for the device
     * @return List of default MQTT permissions with QoS level configurations
     */
    private List<MqttPermission> createDefaultPermissions(String clientId) {
        List<MqttPermission> permissions = new ArrayList<>();
        
        // Default QoS levels: 0 (At most once), 1 (At least once), 2 (Exactly once)
        List<Integer> defaultQosLevels = Arrays.asList(0, 1, 2);
        
        // Permission for telemetry topic (PUBLISH)
        permissions.add(MqttPermission.builder()
            .topicPattern("sensor/" + clientId + "/telemetry")
            .action(MqttAction.PUBLISH)
            .permission(Permission.ALLOW)
            .allowedQosLevels(defaultQosLevels)
            .build());
        
        // Permission for status topic (PUBLISH) - for LWT messages
        permissions.add(MqttPermission.builder()
            .topicPattern("sensor/" + clientId + "/status")
            .action(MqttAction.PUBLISH)
            .permission(Permission.ALLOW)
            .allowedQosLevels(defaultQosLevels)
            .build());
        
        // Permission for command topic (SUBSCRIBE)
        permissions.add(MqttPermission.builder()
            .topicPattern("sensor/" + clientId + "/command")
            .action(MqttAction.SUBSCRIBE)
            .permission(Permission.ALLOW)
            .allowedQosLevels(defaultQosLevels)
            .build());
        
        return permissions;
    }
}
