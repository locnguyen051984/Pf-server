package com.Pf.product_service.repository;

import com.Pf.product_service.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorRepository extends JpaRepository<Color, Long> {
}