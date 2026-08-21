package com.Pf.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class VariantResponse {

    private Long id;
    private Long colorId;
    private String colorName;
    private Long sizeId;
    private String sizeName;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
}