package com.ciro.phonestore.controller;

import com.ciro.phonestore.models.Faq;
import com.ciro.phonestore.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faqs")
@CrossOrigin(origins = "*")
public class FaqController {

    @Autowired
    private FaqRepository faqRepository;

    // Create
    @PostMapping
    public ResponseEntity<?> createFaq(@RequestBody Faq faq) {
        try {
            Faq savedFaq = faqRepository.save(faq);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFaq);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create FAQ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Read all published
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedFaqs() {
        try {
            List<Faq> faqs = faqRepository.findByStatusAndActiveTrue("Published");
            return ResponseEntity.ok(faqs);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to fetch FAQs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Read all (admin)
    @GetMapping
    public ResponseEntity<?> getAllActiveFaqs() {
        try {
            List<Faq> faqs = faqRepository.findByActiveTrue();
            return ResponseEntity.ok(faqs);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to fetch FAQs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFaq(@PathVariable Long id, @RequestBody Faq updatedFaq) {
        try {
            Faq faq = faqRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("FAQ not found"));

            faq.setQuestion(updatedFaq.getQuestion());
            faq.setAnswer(updatedFaq.getAnswer());
            faq.setCategory(updatedFaq.getCategory());
            faq.setStatus(updatedFaq.getStatus());

            Faq savedFaq = faqRepository.save(faq);
            return ResponseEntity.ok(savedFaq);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update FAQ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Soft Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFaq(@PathVariable Long id) {
        try {
            Faq faq = faqRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("FAQ not found"));

            faq.setActive(false);
            faqRepository.save(faq);

            Map<String, String> response = new HashMap<>();
            response.put("message", "FAQ deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete FAQ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
