package com.Pf.product_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank
    private String name;

    private String description;

    private String origin;

    @NotNull
    private Long categoryId;

    @NotEmpty
    @Valid
    private List<VariantRequest> variants;

    @Valid
    private List<ImageRequest> images;
}