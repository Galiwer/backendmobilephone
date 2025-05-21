package com.ciro.phonestore.services;

import com.ciro.phonestore.models.Firmware;
import com.ciro.phonestore.models.FirmwareRequestDTO;
import com.ciro.phonestore.models.FirmwareResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FirmwareService {

    private final FirmwareRepository firmwareRepository;

    @Autowired
    public FirmwareService(FirmwareRepository firmwareRepository) {
        this.firmwareRepository = firmwareRepository;
    }

    public FirmwareResponseDTO createFirmware(FirmwareRequestDTO requestDTO) {
        Firmware firmware = new Firmware();
        firmware.setBrand(requestDTO.getBrand());
        firmware.setModel(requestDTO.getModel());
        firmware.setVersion(requestDTO.getVersion());
        firmware.setFirmwareLink(requestDTO.getFirmwareLink());
        firmware.setReleaseNotes(requestDTO.getReleaseNotes());
        firmware.setUploadDate(LocalDateTime.now());

        firmware = firmwareRepository.save(firmware);
        return convertToResponseDTO(firmware);
    }

    public void deleteFirmware(Long id) {
        firmwareRepository.deleteById(id);
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
        responseDTO.setFirmwareLink(firmware.getFirmwareLink());
        responseDTO.setUploadDate(firmware.getUploadDate());
        responseDTO.setReleaseDate("OS " + firmware.getUploadDate().getYear());
        responseDTO.setReleaseNotes(firmware.getReleaseNotes());
        return responseDTO;
    }
}