package com.ciro.phonestore.controller;

import com.ciro.phonestore.models.Faq;
import com.ciro.phonestore.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@CrossOrigin(origins = "*")
public class FaqController {

    @Autowired
    private FaqRepository faqRepository;

    // Create
    @PostMapping
    public Faq createFaq(@RequestBody Faq faq) {
        return faqRepository.save(faq);
    }

    // Read all published
    @GetMapping("/published")
    public List<Faq> getPublishedFaqs() {
        return faqRepository.findByStatusAndActiveTrue("Published");
    }

    // Read all (admin)
    @GetMapping
    public List<Faq> getAllActiveFaqs() {
        return faqRepository.findByActiveTrue();
    }

    // Update
    @PutMapping("/{id}")
    public Faq updateFaq(@PathVariable Long id, @RequestBody Faq updatedFaq) {
        return faqRepository.findById(id).map(faq -> {
            faq.setQuestion(updatedFaq.getQuestion());
            faq.setAnswer(updatedFaq.getAnswer());
            faq.setCategory(updatedFaq.getCategory());
            faq.setStatus(updatedFaq.getStatus());
            return faqRepository.save(faq);
        }).orElseThrow(() -> new RuntimeException("FAQ not found"));
    }

    // Soft Delete
    @DeleteMapping("/{id}")
    public void deleteFaq(@PathVariable Long id) {
        faqRepository.findById(id).ifPresent(faq -> {
            faq.setActive(false);
            faqRepository.save(faq);
        });
    }
}
