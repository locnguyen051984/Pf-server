package com.Pf.product_service.dto.response;

import com.Pf.product_service.entity.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    private Long id;
    private String name;
    private String origin;
    private Long categoryId;
    private String categoryName;
    private ProductStatus status;
    private LocalDateTime createdAt;
}