package com.ciro.phonestore.services;

import com.ciro.phonestore.models.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, String> {
    Optional<Job> findByJobNumber(String jobNumber);
}