package com.ciro.phonestore.services;

import com.ciro.phonestore.models.Firmware;
import com.ciro.phonestore.models.FirmwareRequestDTO;
import com.ciro.phonestore.models.FirmwareResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FirmwareService {

    private final FirmwareRepository firmwareRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public FirmwareService(FirmwareRepository firmwareRepository, FileStorageService fileStorageService) {
        this.firmwareRepository = firmwareRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public FirmwareResponseDTO createFirmware(FirmwareRequestDTO requestDTO) {
        try {
            // Store the file and get its filename
            String filename = fileStorageService.storeFile(requestDTO.getFirmwareFile());

            // Create and save the firmware entity
            Firmware firmware = new Firmware();
            firmware.setBrand(requestDTO.getBrand());
            firmware.setModel(requestDTO.getModel());
            firmware.setVersion(requestDTO.getVersion());
            firmware.setFirmwareLink(filename); // Store the filename
            firmware.setReleaseNotes(requestDTO.getReleaseNotes());
            firmware.setUploadDate(LocalDateTime.now());

            firmware = firmwareRepository.save(firmware);
            return convertToResponseDTO(firmware);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store firmware file", e);
        }
    }

    @Transactional
    public void deleteFirmware(Long id) {
        Firmware firmware = firmwareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Firmware not found"));

        try {
            // Delete the file first
            fileStorageService.deleteFile(firmware.getFirmwareLink());
            // Then delete the database record
            firmwareRepository.deleteById(id);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete firmware file", e);
        }
    }

    public List<FirmwareResponseDTO> getAllFirmware() {
        List<Firmware> firmwares = firmwareRepository.findAll();
        return firmwares.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<String> getAllBrands() {
        return firmwareRepository.findAllBrands();
    }

    public List<String> getModelsByBrand(String brand) {
        return firmwareRepository.findModelsByBrand(brand);
    }

    public List<FirmwareResponseDTO> getFirmwareByBrandAndModel(String brand, String model) {
        List<Firmware> firmwares = firmwareRepository.findByBrandAndModel(brand, model);
        return firmwares.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private FirmwareResponseDTO convertToResponseDTO(Firmware firmware) {
        FirmwareResponseDTO responseDTO = new FirmwareResponseDTO();
        responseDTO.setId(firmware.getId());
        responseDTO.setBrand(firmware.getBrand());
        responseDTO.setModel(firmware.getModel());
        responseDTO.setVersion(firmware.getVersion());
        responseDTO.setFirmwareLink("/api/firmware/download/" + firmware.getFirmwareLink());
        responseDTO.setUploadDate(firmware.getUploadDate());
        responseDTO.setReleaseDate("OS " + firmware.getUploadDate().getYear());
        responseDTO.setReleaseNotes(firmware.getReleaseNotes());
        return responseDTO;
    }
}