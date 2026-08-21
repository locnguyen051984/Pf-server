package com.Pf.product_service.controller;

import com.Pf.product_service.dto.request.UpdateVariantRequest;
import com.Pf.product_service.dto.response.VariantResponse;
import com.Pf.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
public class VariantController {

    private final ProductService productService;

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VariantResponse> updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVariantRequest request) {
        return ResponseEntity.ok(productService.updateVariant(id, request));
    }
}