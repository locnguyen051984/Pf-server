package com.Pf.product_service.repository;

import com.Pf.product_service.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SizeRepository extends JpaRepository<Size, Long> {
}