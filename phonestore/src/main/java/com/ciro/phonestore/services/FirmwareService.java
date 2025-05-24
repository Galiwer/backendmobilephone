package com.ciro.phonestore.services;

import com.ciro.phonestore.models.Firmware;
import com.ciro.phonestore.repository.FirmwareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FirmwareService {
    private static final Logger logger = LoggerFactory.getLogger(FirmwareService.class);

    @Autowired
    private FirmwareRepository firmwareRepository;

    @Transactional
    public Firmware saveFirmware(Firmware firmware) {
        try {
            logger.info("Saving firmware: {} {} {}", firmware.getBrand(), firmware.getModel(), firmware.getVersion());
            return firmwareRepository.save(firmware);
        } catch (Exception e) {
            logger.error("Error saving firmware", e);
            throw new RuntimeException("Failed to save firmware: " + e.getMessage());
        }
    }

    public Firmware getFirmware(Long id) {
        return firmwareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Firmware not found with id: " + id));
    }

    public List<Firmware> getAllFirmware() {
        return firmwareRepository.findByActiveTrue();
    }

    public List<Firmware> getFirmwareByBrandAndModel(String brand, String model) {
        return firmwareRepository.findByBrandAndModelAndActiveTrue(brand, model);
    }

    public List<String> getAllBrands() {
        return firmwareRepository.findDistinctBrandsByActiveTrue();
    }

    public List<String> getModelsByBrand(String brand) {
        return firmwareRepository.findDistinctModelsByBrandAndActiveTrue(brand);
    }

    @Transactional
    public void deleteFirmware(Long id) {
        try {
            Firmware firmware = getFirmware(id);
            firmware.setActive(false);
            firmwareRepository.save(firmware);
            logger.info("Firmware soft deleted: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting firmware", e);
            throw new RuntimeException("Failed to delete firmware: " + e.getMessage());
        }
    }
}