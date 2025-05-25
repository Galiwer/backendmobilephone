package com.ciro.phonestore.controller;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ciro.phonestore.models.Product;
import com.ciro.phonestore.models.ProductDto;
import com.ciro.phonestore.repository.ProductsRepository;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private static final String UPLOAD_DIR = "public/images/";
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductsRepository repo;

    @GetMapping("/list")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = repo.findAll();
            logger.debug("Retrieved {} products", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error retrieving products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        try {
            Optional<Product> productOpt = repo.findById(id);
            if (productOpt.isPresent()) {
                logger.debug("Retrieved product with id: {}", id);
                return ResponseEntity.ok(productOpt.get());
            } else {
                logger.debug("Product not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving product with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@ModelAttribute ProductDto productDto) {
        logger.info("Received request to create product with data: name={}, brand={}, category={}",
                productDto.getName(), productDto.getBrand(), productDto.getCategory());

        // Validate required fields
        if (productDto.getName() == null || productDto.getName().trim().isEmpty()) {
            logger.error("Product creation failed: Name is required");
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }

        if (productDto.getBrand() == null || productDto.getBrand().trim().isEmpty()) {
            logger.error("Product creation failed: Brand is required");
            return ResponseEntity.badRequest().body(Map.of("error", "Brand is required"));
        }

        if (productDto.getCategory() == null || productDto.getCategory().trim().isEmpty()) {
            logger.error("Product creation failed: Category is required");
            return ResponseEntity.badRequest().body(Map.of("error", "Category is required"));
        }

        if (productDto.getImageFile() == null || productDto.getImageFile().isEmpty()) {
            logger.error("Product creation failed: Image file is required");
            return ResponseEntity.badRequest().body(Map.of("error", "Image file is required"));
        }

        try {
            MultipartFile image = productDto.getImageFile();
            Date createdAt = new Date();
            String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
            logger.debug("Processing image file: {}", storageFileName);


            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                logger.debug("Creating upload directory: {}", UPLOAD_DIR);
                Files.createDirectories(uploadPath);
            }


            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Image file saved successfully: {}", storageFileName);
            }


            Product product = new Product();
            product.setName(productDto.getName().trim());
            product.setBrand(productDto.getBrand().trim());
            product.setCategory(productDto.getCategory().trim());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription() != null ? productDto.getDescription().trim() : "");
            product.setCreatedAt(createdAt);
            product.setImageFileName(storageFileName);

            Product savedProduct = repo.save(product);
            logger.info("Product created successfully with ID: {}", savedProduct.getId());
            return ResponseEntity.ok(savedProduct);
        } catch (Exception ex) {
            logger.error("Error creating product: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create product: " + ex.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @ModelAttribute ProductDto productDto) {
        try {
            Optional<Product> productOpt = repo.findById(id);
            if (productOpt.isEmpty()) {
                logger.debug("Product not found for update with id: {}", id);
                return ResponseEntity.notFound().build();
            }

            Product product = productOpt.get();

            if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {

                Path oldImagePath = Paths.get(UPLOAD_DIR + product.getImageFileName());
                Files.deleteIfExists(oldImagePath);


                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(UPLOAD_DIR + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageFileName(storageFileName);
            }


            if (productDto.getName() != null)
                product.setName(productDto.getName().trim());
            if (productDto.getBrand() != null)
                product.setBrand(productDto.getBrand().trim());
            if (productDto.getCategory() != null)
                product.setCategory(productDto.getCategory().trim());
            if (productDto.getPrice() > 0)
                product.setPrice(productDto.getPrice());
            if (productDto.getDescription() != null)
                product.setDescription(productDto.getDescription().trim());

            Product updatedProduct = repo.save(product);
            logger.info("Product updated successfully with ID: {}", updatedProduct.getId());
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception ex) {
            logger.error("Error updating product with id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to update product: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        try {
            Optional<Product> productOpt = repo.findById(id);
            if (productOpt.isEmpty()) {
                logger.debug("Product not found for deletion with id: {}", id);
                return ResponseEntity.notFound().build();
            }

            Product product = productOpt.get();
            Path imagePath = Paths.get(UPLOAD_DIR + product.getImageFileName());
            Files.deleteIfExists(imagePath);
            repo.delete(product);

            logger.info("Product deleted successfully with ID: {}", id);
            return ResponseEntity.ok().body(Map.of("message", "Product deleted successfully"));
        } catch (Exception ex) {
            logger.error("Error deleting product with id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete product: " + ex.getMessage()));
        }
    }
}