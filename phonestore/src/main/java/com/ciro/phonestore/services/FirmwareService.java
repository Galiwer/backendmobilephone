package com.ciro.phonestore.services;

import com.ciro.phonestore.models.Firmware;
import com.ciro.phonestore.repository.FirmwareRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
            validateFirmware(firmware);
            Firmware savedFirmware = firmwareRepository.save(firmware);
            logger.info("Successfully saved firmware with ID: {}", savedFirmware.getId());
            return savedFirmware;
        } catch (DataAccessException e) {
            logger.error("Database error while saving firmware", e);
            throw new RuntimeException("Database error occurred while saving firmware", e);
        } catch (Exception e) {
            logger.error("Error saving firmware", e);
            throw new RuntimeException("Failed to save firmware: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Firmware getFirmware(Long id) {
        try {
            logger.info("Retrieving firmware with ID: {}", id);
            Firmware firmware = firmwareRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Firmware not found with id: " + id));
            logger.info("Successfully retrieved firmware with ID: {}", id);
            return firmware;
        } catch (EntityNotFoundException e) {
            logger.error("Firmware not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving firmware with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve firmware: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Firmware> getAllFirmware() {
        try {
            logger.info("Retrieving all active firmware entries");
            List<Firmware> firmwareList = firmwareRepository.findByActiveTrue();
            logger.info("Successfully retrieved {} firmware entries", firmwareList.size());
            return firmwareList;
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving firmware list", e);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error retrieving firmware list", e);
            throw new RuntimeException("Failed to retrieve firmware list: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Firmware> getFirmwareByBrandAndModel(String brand, String model) {
        try {
            logger.info("Retrieving firmware for brand: {} and model: {}", brand, model);
            List<Firmware> firmwareList = firmwareRepository.findByBrandAndModelOrderByCreatedAtDesc(brand, model);
            logger.info("Found {} firmware entries for brand: {} and model: {}", firmwareList.size(), brand, model);
            return firmwareList;
        } catch (Exception e) {
            logger.error("Error retrieving firmware for brand: {} and model: {}", brand, model, e);
            throw new RuntimeException("Failed to retrieve firmware by brand and model", e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        try {
            logger.info("Retrieving all firmware brands");
            List<String> brands = firmwareRepository.findDistinctBrandsByActiveTrue();
            logger.info("Successfully retrieved {} brands", brands.size());
            return brands;
        } catch (Exception e) {
            logger.error("Error retrieving firmware brands", e);
            throw new RuntimeException("Failed to retrieve firmware brands", e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getModelsByBrand(String brand) {
        try {
            logger.info("Retrieving models for brand: {}", brand);
            List<String> models = firmwareRepository.findDistinctModelsByBrandAndActiveTrue(brand);
            logger.info("Successfully retrieved {} models for brand: {}", models.size(), brand);
            return models;
        } catch (Exception e) {
            logger.error("Error retrieving models for brand: {}", brand, e);
            throw new RuntimeException("Failed to retrieve models for brand", e);
        }
    }

    @Transactional
    public void deleteFirmware(Long id) {
        try {
            logger.info("Attempting to delete firmware with ID: {}", id);
            Firmware firmware = getFirmware(id);
            firmware.setActive(false);
            firmwareRepository.save(firmware);
            logger.info("Successfully soft deleted firmware with ID: {}", id);
        } catch (EntityNotFoundException e) {
            logger.error("Cannot delete firmware: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting firmware with ID: {}", id, e);
            throw new RuntimeException("Failed to delete firmware: " + e.getMessage(), e);
        }
    }

    private void validateFirmware(Firmware firmware) {
        if (firmware.getBrand() == null || firmware.getBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Brand cannot be empty");
        }
        if (firmware.getModel() == null || firmware.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be empty");
        }
        if (firmware.getVersion() == null || firmware.getVersion().trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be empty");
        }
        if (firmware.getFileName() == null && firmware.getFirmwareLink() == null) {
            throw new IllegalArgumentException("Either file name or firmware link must be provided");
        }
    }
}