package com.ciro.phonestore.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ciro.phonestore.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer> {
    


    
} 
