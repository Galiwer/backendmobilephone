package com.ciro.phonestore.controller;

import com.ciro.phonestore.models.FirmwareRequestDTO;
import com.ciro.phonestore.models.FirmwareResponseDTO;
import com.ciro.phonestore.services.FirmwareService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/firmware")
@CrossOrigin(origins = "*")
public class FirmwareController {

    private final FirmwareService firmwareService;

    @Autowired
    public FirmwareController(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @PostMapping
    public ResponseEntity<FirmwareResponseDTO> createFirmware(@RequestBody FirmwareRequestDTO requestDTO) {
        FirmwareResponseDTO responseDTO = firmwareService.createFirmware(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        List<String> brands = firmwareService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFirmware(@PathVariable Long id) {
        try {
            firmwareService.deleteFirmware(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Firmware deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to delete firmware: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<List<FirmwareResponseDTO>> getAllFirmware() {
        List<FirmwareResponseDTO> firmwares = firmwareService.getAllFirmware();
        return ResponseEntity.ok(firmwares);
    }

    @GetMapping("/models/{brand}")
    public ResponseEntity<List<String>> getModelsByBrand(@PathVariable String brand) {
        List<String> models = firmwareService.getModelsByBrand(brand);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/{brand}/{model}")
    public ResponseEntity<List<FirmwareResponseDTO>> getFirmwareVersions(
            @PathVariable String brand,
            @PathVariable String model) {
        List<FirmwareResponseDTO> firmwares = firmwareService.getFirmwareByBrandAndModel(brand, model);
        return ResponseEntity.ok(firmwares);
    }

    @GetMapping("/device-data")
    public ResponseEntity<Map<String, Object>> getDeviceData() {
        Map<String, Object> response = new HashMap<>();

        List<String> brands = firmwareService.getAllBrands();
        response.put("brands", brands);

        // Create a data structure similar to what the frontend expects
        Map<String, Object> firmwareData = new HashMap<>();

        for (String brand : brands) {
            Map<String, Object> brandData = new HashMap<>();
            List<String> models = firmwareService.getModelsByBrand(brand);

            brandData.put("models", models);

            Map<String, List<FirmwareResponseDTO>> versions = new HashMap<>();
            for (String model : models) {
                List<FirmwareResponseDTO> firmwares = firmwareService.getFirmwareByBrandAndModel(brand, model);
                versions.put(model, firmwares);
            }

            brandData.put("versions", versions);
            firmwareData.put(brand, brandData);
        }

        response.put("firmwareData", firmwareData);
        return ResponseEntity.ok(response);
    }
}
