package com.Pf.product_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateVariantRequest {

    private String sku;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    private Long colorId;
    private Long sizeId;
}