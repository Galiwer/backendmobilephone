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
@CrossOrigin(origins = "*")
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

        logger.info("Received firmware upload request for {}, {}, version {}", brand, model, version);

        try {
            if (firmwareFile == null && (firmwareLink == null || firmwareLink.isEmpty())) {
                logger.error("Neither firmware file nor link provided");
                throw new IllegalArgumentException("Either firmware file or Google Drive link must be provided");
            }

            Firmware firmware = new Firmware();
            firmware.setBrand(brand);
            firmware.setModel(model);
            firmware.setVersion(version);
            firmware.setReleaseNotes(releaseNotes);

            if (firmwareFile != null && !firmwareFile.isEmpty()) {
                logger.debug("Processing firmware file upload");
                String fileName = fileStorageService.storeFile(firmwareFile);
                firmware.setFileName(fileName);
                logger.info("Firmware file stored successfully: {}", fileName);
            } else if (firmwareLink != null && !firmwareLink.isEmpty()) {
                logger.debug("Processing firmware link");
                if (!isValidDriveLink(firmwareLink)) {
                    logger.error("Invalid Google Drive link provided: {}", firmwareLink);
                    throw new IllegalArgumentException("Invalid Google Drive link format");
                }
                firmware.setFirmwareLink(firmwareLink);
                logger.info("Firmware link stored successfully");
            }

            Firmware savedFirmware = firmwareService.saveFirmware(firmware);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedFirmware.getId());
            response.put("brand", savedFirmware.getBrand());
            response.put("model", savedFirmware.getModel());
            response.put("version", savedFirmware.getVersion());
            response.put("type", firmwareFile != null ? "file" : "link");
            response.put("downloadUrl", String.format("/api/firmware/download/%d", savedFirmware.getId()));

            logger.info("Firmware entry saved successfully with ID: {}", savedFirmware.getId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error during firmware upload: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error uploading firmware", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to upload firmware: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFirmware(@PathVariable Long id) {
        logger.info("Received firmware download request for ID: {}", id);
        try {
            Firmware firmware = firmwareService.getFirmware(id);

            // For Google Drive links
            if (firmware.getFirmwareLink() != null && !firmware.getFirmwareLink().isEmpty()) {
                logger.info("Returning Google Drive link for firmware ID: {}", id);
                Map<String, Object> response = new HashMap<>();
                response.put("type", "link");
                response.put("url", firmware.getFirmwareLink());
                response.put("fileName", String.format("%s_%s_firmware.zip", firmware.getBrand(), firmware.getModel()));
                return ResponseEntity.ok(response);
            }

            // For direct file downloads
            if (firmware.getFileName() == null || firmware.getFileName().isEmpty()) {
                logger.error("No file or link found for firmware ID: {}", id);
                throw new RuntimeException("No firmware file or link available");
            }

            String fileName = String.format("%s_%s_firmware.zip", firmware.getBrand(), firmware.getModel());
            Map<String, Object> response = new HashMap<>();
            response.put("type", "file");
            response.put("url", String.format("/api/firmware/file/%d", firmware.getId()));
            response.put("fileName", fileName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error downloading firmware with ID: {}", id, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to download firmware: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> downloadFirmwareFile(@PathVariable Long id) {
        logger.info("Received firmware file download request for ID: {}", id);
        try {
            Firmware firmware = firmwareService.getFirmware(id);

            if (firmware.getFileName() == null || firmware.getFileName().isEmpty()) {
                logger.error("No file found for firmware ID: {}", id);
                throw new RuntimeException("No firmware file available");
            }

            Resource resource = fileStorageService.loadFileAsResource(firmware.getFileName());
            String fileName = String.format("%s_%s_firmware.zip", firmware.getBrand(), firmware.getModel());
            String contentType = "application/zip";
            String contentDisposition = "attachment; filename=\"" + fileName + "\"";

            logger.info("Serving firmware file: {} for ID: {}", fileName, id);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error downloading firmware file with ID: {}", id, e);
            throw new RuntimeException("Could not download firmware file", e);
        }
    }

    @GetMapping("/brands")
    public ResponseEntity<?> getAllBrands() {
        try {
            List<String> brands = firmwareService.getAllBrands();
            logger.info("Retrieved {} firmware brands", brands.size());
            return ResponseEntity.ok(brands);
        } catch (Exception e) {
            logger.error("Error retrieving firmware brands", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve brands: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/models/{brand}")
    public ResponseEntity<?> getModelsByBrand(@PathVariable String brand) {
        try {
            List<String> models = firmwareService.getModelsByBrand(brand);
            logger.info("Retrieved {} models for brand: {}", models.size(), brand);
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            logger.error("Error retrieving models for brand: {}", brand, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve models: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFirmware(@PathVariable Long id) {
        logger.info("Received delete request for firmware ID: {}", id);
        try {
            Firmware firmware = firmwareService.getFirmware(id);

            if (firmware.getFileName() != null && !firmware.getFileName().isEmpty()) {
                logger.debug("Deleting firmware file: {}", firmware.getFileName());
                fileStorageService.deleteFile(firmware.getFileName());
            }

            firmwareService.deleteFirmware(id);
            logger.info("Successfully deleted firmware with ID: {}", id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Firmware deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting firmware with ID: {}", id, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete firmware: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/admin/list")
    public ResponseEntity<?> getAllFirmware() {
        logger.info("Retrieving all firmware entries");
        try {
            List<Firmware> firmwareList = firmwareService.getAllFirmware();
            logger.info("Successfully retrieved {} firmware entries", firmwareList.size());
            return ResponseEntity.ok(firmwareList);
        } catch (Exception e) {
            logger.error("Error retrieving firmware list", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve firmware list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> getFirmwareDetails(@PathVariable Long id) {
        logger.info("Retrieving firmware details for ID: {}", id);
        try {
            Firmware firmware = firmwareService.getFirmware(id);
            logger.info("Successfully retrieved firmware details for ID: {}", id);
            return ResponseEntity.ok(firmware);
        } catch (Exception e) {
            logger.error("Error retrieving firmware details for ID: {}", id, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve firmware details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/list/{brand}/{model}")
    public ResponseEntity<?> getFirmwareByBrandAndModel(
            @PathVariable String brand,
            @PathVariable String model) {
        logger.info("Retrieving firmware list for brand: {} and model: {}", brand, model);
        try {
            List<Firmware> firmwareList = firmwareService.getFirmwareByBrandAndModel(brand, model);
            return ResponseEntity.ok(firmwareList);
        } catch (Exception e) {
            logger.error("Error retrieving firmware list for brand: {} and model: {}", brand, model, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve firmware list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private boolean isValidDriveLink(String link) {
        return link != null && (link.startsWith("https://drive.google.com/") ||
                link.startsWith("https://docs.google.com/") ||
                link.startsWith("http://drive.google.com/") ||
                link.startsWith("http://docs.google.com/"));
    }
}
