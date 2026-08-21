package com.Pf.product_service.mapper;

import com.Pf.product_service.dto.response.ImageResponse;
import com.Pf.product_service.dto.response.ProductListResponse;
import com.Pf.product_service.dto.response.ProductResponse;
import com.Pf.product_service.dto.response.VariantResponse;
import com.Pf.product_service.entity.Product;
import com.Pf.product_service.entity.ProductImage;
import com.Pf.product_service.entity.ProductVariant;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    default ProductResponse toResponse(Product product, List<ProductVariant> variants, List<ProductImage> images) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .origin(product.getOrigin())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .status(product.getStatus())
                .variants(variants.stream().map(this::toVariantResponse).toList())
                .images(images.stream().map(this::toImageResponse).toList())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    default VariantResponse toVariantResponse(ProductVariant variant) {
        return VariantResponse.builder()
                .id(variant.getId())
                .colorId(variant.getColor().getId())
                .colorName(variant.getColor().getName())
                .sizeId(variant.getSize().getId())
                .sizeName(variant.getSize().getName())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .quantity(variant.getQuantity())
                .build();
    }

    default ImageResponse toImageResponse(ProductImage image) {
        return ImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
    default ProductListResponse toListResponse(Product product) {
    return ProductListResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .origin(product.getOrigin())
            .categoryId(product.getCategory().getId())
            .categoryName(product.getCategory().getName())
            .status(product.getStatus())
            .createdAt(product.getCreatedAt())
            .build();
}
}