package com.ciro.phonestore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.upload.dir:/tmp/firmware-uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("File storage location initialized at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            logger.error("Could not create directory at {}", this.fileStorageLocation, ex);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.error("Failed to store file. File is null or empty");
            throw new RuntimeException("Failed to store empty file.");
        }

        try {
            // Generate unique filename
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Validate file name
            if (newFileName.contains("..")) {
                logger.error("Invalid file name: {}", newFileName);
                throw new RuntimeException("Invalid file name");
            }

            // Create the file path
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);

            // Copy the file, replacing existing files of the same name
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Successfully stored file {} as {}", originalFileName, newFileName);
            return newFileName;
        } catch (IOException ex) {
            logger.error("Failed to store file {}", file.getOriginalFilename(), ex);
            throw new RuntimeException("Failed to store file.", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            if (fileName == null || fileName.isEmpty()) {
                logger.error("Failed to load file. Filename is null or empty");
                throw new RuntimeException("Filename cannot be empty.");
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                logger.info("File found: {}", fileName);
                return resource;
            } else {
                logger.error("File not found: {}", fileName);
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            logger.error("File not found: {}", fileName, ex);
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            if (fileName == null || fileName.isEmpty()) {
                logger.error("Failed to delete file. Filename is null or empty");
                return;
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                logger.info("Successfully deleted file: {}", fileName);
            } else {
                logger.warn("File {} did not exist for deletion", fileName);
            }
        } catch (IOException ex) {
            logger.error("Failed to delete file: {}", fileName, ex);
            throw new RuntimeException("Failed to delete file: " + fileName, ex);
        }
    }
}