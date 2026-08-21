package com.Pf.product_service.repository;

import com.Pf.product_service.entity.Product;
import com.Pf.product_service.entity.ProductStatus;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // JOIN FETCH category vi la ManyToOne (khong phai collection) -> khong gay
    // Cartesian Product
    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category WHERE p.status <> :excludedStatus", countQuery = "SELECT count(p) FROM Product p WHERE p.status <> :excludedStatus")
    Page<Product> findAllNotStatus(ProductStatus excludedStatus, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id AND p.status <> :excludedStatus")
    Optional<Product> findByIdNotStatus(Long id, ProductStatus excludedStatus);
}