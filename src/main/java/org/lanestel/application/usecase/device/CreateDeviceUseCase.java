package org.lanestel.application.usecase.device;

import org.lanestel.application.exception.mqtt.MqttAccountAlreadyExistsException;
import org.lanestel.common.utils.password_util.IPasswordUtil;
import org.lanestel.common.utils.uid_util.IUidUtil;
import org.lanestel.domain.entity.device.Device;
import org.lanestel.domain.entity.mqtt_account.MqttAccount;
import org.lanestel.domain.pojo.device.DevicePOJO;
import org.lanestel.domain.repository.IDeviceRepository;
import org.lanestel.domain.repository.IMqttRepository;
import org.lanestel.domain.usecase.device.ICreateDeviceUseCase;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
     * 2. Generates a unique device UID and secure password
     * 3. Creates MQTT account and device entities
     * 4. Persists the device to the repository
     * 5. Returns a DevicePOJO with the raw password for initial setup
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
            String deviceUid = uidUtil.generateUid();
            String  rawPassword = passwordUtil.generatePassword(8);

            MqttAccount mqttAccount = MqttAccount.builder()
                    .mqttId(mqttUsername)
                    .mqttPassword(rawPassword)
                    .clientId(deviceUid)
                    .build();
            Device newDevice = Device.builder()
                    .deviceName(deviceName)
                    .mqttAccount(mqttAccount)
                    .build();
            // Create device and return POJO with raw password
            return Uni.createFrom().item(deviceRepository.createDevice(newDevice)).onItem().transform(device ->{
                DevicePOJO devicePOJO = device.toPojo();
                // Set raw password instead of hashed password for response
                devicePOJO.setMqttPassword(rawPassword);
                return devicePOJO;
            });
       });
    }
    
}
