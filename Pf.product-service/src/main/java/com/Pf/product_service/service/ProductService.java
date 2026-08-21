package com.Pf.product_service.service;

import com.Pf.product_service.dto.request.CreateProductRequest;
import com.Pf.product_service.dto.request.ImageRequest;
import com.Pf.product_service.dto.request.VariantRequest;
import com.Pf.product_service.dto.response.ProductListResponse;
import com.Pf.product_service.dto.response.ProductResponse;
import com.Pf.product_service.entity.*;
import com.Pf.product_service.exception.ResourceNotFoundException;
import com.Pf.product_service.mapper.ProductMapper;
import com.Pf.product_service.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

        private final ProductRepository productRepository;
        private final ProductVariantRepository variantRepository;
        private final ProductImageRepository imageRepository;
        private final CategoryRepository categoryRepository;
        private final ColorRepository colorRepository;
        private final SizeRepository sizeRepository;
        private final ProductMapper productMapper;

        @Transactional
        public ProductResponse createProduct(CreateProductRequest request) {
                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Category not found: " + request.getCategoryId()));

                Product newProduct = Product.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .origin(request.getOrigin())
                                .category(category)
                                .status(ProductStatus.ACTIVE)
                                .build();
                final Product savedProduct = productRepository.save(newProduct);

                List<ProductVariant> variants = request.getVariants().stream()
                                .map(v -> toVariantEntity(v, savedProduct))
                                .toList();
                variants = variantRepository.saveAll(variants);

                List<ProductImage> images = request.getImages() == null
                                ? List.of()
                                : request.getImages().stream()
                                                .map(img -> toImageEntity(img, savedProduct))
                                                .toList();
                if (!images.isEmpty()) {
                        images = imageRepository.saveAll(images);
                }

                return productMapper.toResponse(savedProduct, variants, images);
        }

        private ProductVariant toVariantEntity(VariantRequest req, Product product) {
                Color color = colorRepository.findById(req.getColorId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Color not found: " + req.getColorId()));
                Size size = sizeRepository.findById(req.getSizeId())
                                .orElseThrow(() -> new ResourceNotFoundException("Size not found: " + req.getSizeId()));

                return ProductVariant.builder()
                                .product(product)
                                .color(color)
                                .size(size)
                                .sku(req.getSku())
                                .price(req.getPrice())
                                .quantity(req.getQuantity())
                                .build();
        }

        private ProductImage toImageEntity(ImageRequest req, Product product) {
                return ProductImage.builder()
                                .product(product)
                                .imageUrl(req.getImageUrl())
                                .displayOrder(req.getDisplayOrder() == null ? 0 : req.getDisplayOrder())
                                .build();
        }

        public Page<ProductListResponse> listProducts(Pageable pageable) {
                return productRepository.findAllNotStatus(ProductStatus.DELETED, pageable)
                                .map(productMapper::toListResponse);
        }

        public ProductResponse getProductDetail(Long id) {
                Product product = productRepository.findByIdNotStatus(id, ProductStatus.DELETED)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

                List<ProductVariant> variants = variantRepository.findByProductId(id);
                List<ProductImage> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(id);

                return productMapper.toResponse(product, variants, images);
        }

        @Transactional
        public void softDeleteProduct(Long id) {
                Product product = productRepository.findByIdNotStatus(id, ProductStatus.DELETED)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

                product.setStatus(ProductStatus.DELETED);
                productRepository.save(product);
        }
}