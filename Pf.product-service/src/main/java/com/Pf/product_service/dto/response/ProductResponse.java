package com.Pf.product_service.dto.response;

import com.Pf.product_service.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String origin;
    private Long categoryId;
    private String categoryName;
    private ProductStatus status;
    private List<VariantResponse> variants;
    private List<ImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}