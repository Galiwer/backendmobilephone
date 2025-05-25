package com.ciro.phonestore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ciro.phonestore.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer> {


} 
