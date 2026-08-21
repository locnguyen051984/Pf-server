package com.Pf.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageRequest {

    @NotBlank
    private String imageUrl;

    private Integer displayOrder;
}