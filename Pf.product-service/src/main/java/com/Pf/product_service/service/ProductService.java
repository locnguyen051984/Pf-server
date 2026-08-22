package com.Pf.product_service.service;

import com.Pf.product_service.dto.request.CreateProductRequest;
import com.Pf.product_service.dto.request.ImageRequest;
import com.Pf.product_service.dto.request.UpdateProductRequest;
import com.Pf.product_service.dto.request.UpdateVariantRequest;
import com.Pf.product_service.dto.request.VariantRequest;
import com.Pf.product_service.dto.response.ImageResponse;
import com.Pf.product_service.dto.response.ProductListResponse;
import com.Pf.product_service.dto.response.ProductResponse;
import com.Pf.product_service.dto.response.VariantResponse;
import com.Pf.product_service.entity.*;
import com.Pf.product_service.exception.InsufficientStockException;
import com.Pf.product_service.exception.ResourceNotFoundException;
import com.Pf.product_service.mapper.ProductMapper;
import com.Pf.product_service.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
        private final CacheManager cacheManager;

        @Transactional
        @CacheEvict(value = "product:list", allEntries = true)
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

        @Cacheable(value = "product:list", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort", condition = "#pageable.pageNumber == 0 and #pageable.pageSize == 20")
        public Page<ProductListResponse> listProducts(Pageable pageable) {
                return productRepository.findAllNotStatus(ProductStatus.DELETED, pageable)
                                .map(productMapper::toListResponse);
        }

        @Cacheable(value = "product:detail", key = "#id")
        public ProductResponse getProductDetail(Long id) {
                Product product = productRepository.findByIdNotStatus(id, ProductStatus.DELETED)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

                List<ProductVariant> variants = variantRepository.findByProductId(id);
                List<ProductImage> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(id);

                return productMapper.toResponse(product, variants, images);
        }

        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "product:detail", key = "#id"),
                        @CacheEvict(value = "product:list", allEntries = true)
        })
        public void softDeleteProduct(Long id) {
                Product product = productRepository.findByIdNotStatus(id, ProductStatus.DELETED)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

                product.setStatus(ProductStatus.DELETED);
                productRepository.save(product);
        }

        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "product:detail", key = "#id"),
                        @CacheEvict(value = "product:list", allEntries = true)
        })
        public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
                Product product = productRepository.findByIdNotStatus(id, ProductStatus.DELETED)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

                if (request.getName() != null) {
                        product.setName(request.getName());
                }
                if (request.getDescription() != null) {
                        product.setDescription(request.getDescription());
                }
                if (request.getOrigin() != null) {
                        product.setOrigin(request.getOrigin());
                }
                if (request.getCategoryId() != null) {
                        Category category = categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Category not found: " + request.getCategoryId()));
                        product.setCategory(category);
                }

                Product updated = productRepository.save(product);

                List<ProductVariant> variants = variantRepository.findByProductId(id);
                List<ProductImage> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(id);

                return productMapper.toResponse(updated, variants, images);
        }

        @Transactional
        public VariantResponse updateVariant(Long variantId, UpdateVariantRequest request) {
                ProductVariant variant = variantRepository.findById(variantId)
                                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));

                if (request.getSku() != null) {
                        variant.setSku(request.getSku());
                }
                if (request.getPrice() != null) {
                        variant.setPrice(request.getPrice());
                }
                if (request.getColorId() != null) {
                        Color color = colorRepository.findById(request.getColorId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Color not found: " + request.getColorId()));
                        variant.setColor(color);
                }
                if (request.getSizeId() != null) {
                        Size size = sizeRepository.findById(request.getSizeId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Size not found: " + request.getSizeId()));
                        variant.setSize(size);
                }

                ProductVariant updated = variantRepository.save(variant);
                evictProductDetailCache(updated.getProduct().getId());

                return productMapper.toVariantResponse(updated);
        }

        @Transactional
        @CacheEvict(value = "product:detail", key = "#productId")

        public ImageResponse addImage(Long productId, ImageRequest request) {
                Product product = productRepository.findByIdNotStatus(productId, ProductStatus.DELETED)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

                ProductImage image = ProductImage.builder()
                                .product(product)
                                .imageUrl(request.getImageUrl())
                                .displayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder())
                                .build();

                ProductImage saved = imageRepository.save(image);
                return productMapper.toImageResponse(saved);
        }

        @Transactional
        @CacheEvict(value = "product:detail", key = "#productId")

        public void deleteImage(Long productId, Long imageId) {
                ProductImage image = imageRepository.findById(imageId)
                                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

                if (!image.getProduct().getId().equals(productId)) {
                        throw new ResourceNotFoundException("Image not found: " + imageId);
                }

                imageRepository.delete(image);
        }

        @Transactional
        public void decreaseStock(Long variantId, Integer qty) {
                int affectedRows = variantRepository.decreaseStock(variantId, qty);
                if (affectedRows == 0) {
                        throw new InsufficientStockException("Not enough stock or variant not found: " + variantId);
                }
                ProductVariant variant = variantRepository.findById(variantId)
                                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
                evictProductDetailCache(variant.getProduct().getId());
        }

        @Transactional
        public void increaseStock(Long variantId, Integer qty) {
                int affectedRows = variantRepository.increaseStock(variantId, qty);
                if (affectedRows == 0) {
                        throw new ResourceNotFoundException("Variant not found: " + variantId);
                }
                ProductVariant variant = variantRepository.findById(variantId)
                                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
                evictProductDetailCache(variant.getProduct().getId());
        }

        private void evictProductDetailCache(Long productId) {
                var cache = cacheManager.getCache("product:detail");
                if (cache != null) {
                        cache.evict(productId);
                }
        }
}