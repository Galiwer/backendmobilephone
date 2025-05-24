package com.ciro.phonestore.controller;

import com.ciro.phonestore.models.Firmware;
import com.ciro.phonestore.services.FileStorageService;
import com.ciro.phonestore.services.FirmwareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/firmware")
public class FirmwareController {
    private static final Logger logger = LoggerFactory.getLogger(FirmwareController.class);

    @Autowired
    private FirmwareService firmwareService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFirmware(
            @RequestParam("brand") String brand,
            @RequestParam("model") String model,
            @RequestParam("version") String version,
            @RequestParam(value = "releaseNotes", required = false) String releaseNotes,
            @RequestParam(value = "firmwareFile", required = false) MultipartFile firmwareFile,
            @RequestParam(value = "firmwareLink", required = false) String firmwareLink) {
        try {
            if (firmwareFile == null && (firmwareLink == null || firmwareLink.isEmpty())) {
                throw new IllegalArgumentException("Either firmware file or Google Drive link must be provided");
            }

            Firmware firmware = new Firmware();
            firmware.setBrand(brand);
            firmware.setModel(model);
            firmware.setVersion(version);
            firmware.setReleaseNotes(releaseNotes);

            if (firmwareFile != null) {
                String fileName = fileStorageService.storeFile(firmwareFile);
                firmware.setFileName(fileName);
            } else {
                firmware.setFirmwareLink(firmwareLink);
            }

            Firmware savedFirmware = firmwareService.saveFirmware(firmware);
            return ResponseEntity.ok(savedFirmware);
        } catch (Exception e) {
            logger.error("Error uploading firmware", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFirmware(@PathVariable Long id) {
        try {
            Firmware firmware = firmwareService.getFirmware(id);

            // If firmware has a Google Drive link, return it
            if (firmware.getFirmwareLink() != null && !firmware.getFirmwareLink().isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("firmwareLink", firmware.getFirmwareLink());
                return ResponseEntity.ok(response);
            }

            // Otherwise, serve the file
            Resource resource = fileStorageService.loadFileAsResource(firmware.getFileName());
            String contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + firmware.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error downloading firmware", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/admin/list")
    public ResponseEntity<?> getAllFirmware() {
        try {
            List<Firmware> firmwareList = firmwareService.getAllFirmware();
            return ResponseEntity.ok(firmwareList);
        } catch (Exception e) {
            logger.error("Error getting firmware list", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFirmware(@PathVariable Long id) {
        try {
            Firmware firmware = firmwareService.getFirmware(id);

            // Delete the file if it exists
            if (firmware.getFileName() != null) {
                fileStorageService.deleteFile(firmware.getFileName());
            }

            firmwareService.deleteFirmware(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Firmware deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting firmware", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
