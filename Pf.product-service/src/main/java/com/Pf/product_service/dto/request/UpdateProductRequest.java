package com.Pf.product_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductRequest {

    private String name;
    private String description;
    private String origin;
    private Long categoryId;
}