package com.ciro.phonestore.repository;

import com.ciro.phonestore.models.Firmware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirmwareRepository extends JpaRepository<Firmware, Long> {
    List<Firmware> findByActiveTrue();

    List<Firmware> findByBrandAndModelAndActiveTrue(String brand, String model);

    @Query("SELECT DISTINCT f.brand FROM Firmware f WHERE f.active = true")
    List<String> findDistinctBrandsByActiveTrue();

    @Query("SELECT DISTINCT f.model FROM Firmware f WHERE f.brand = ?1 AND f.active = true")
    List<String> findDistinctModelsByBrandAndActiveTrue(String brand);

    @Query("SELECT f FROM Firmware f WHERE f.brand = ?1 AND f.model = ?2 ORDER BY f.id DESC")
    List<Firmware> findByBrandAndModelOrderByCreatedAtDesc(String brand, String model);
}