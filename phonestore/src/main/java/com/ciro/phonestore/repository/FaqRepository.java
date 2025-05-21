package com.ciro.phonestore.repository;

import com.ciro.phonestore.models.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByStatusAndActiveTrue(String status);

    List<Faq> findByActiveTrue();
}