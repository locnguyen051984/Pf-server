package com.Pf.product_service.dto.response;

import com.Pf.product_service.entity.ProductStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProductListResponse {

    private Long id;
    private String name;
    private String origin;
    private Long categoryId;
    private String categoryName;
    private ProductStatus status;
    private LocalDateTime createdAt;
}