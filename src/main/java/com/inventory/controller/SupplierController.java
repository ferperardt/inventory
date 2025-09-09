package com.inventory.controller;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import com.inventory.service.SupplierService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
@Validated
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<SupplierResponse> suppliers = supplierService.getAllSuppliers(pageable);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable UUID id) {
        SupplierResponse supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SupplierResponse>> searchSuppliers(
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @RequestParam(required = false) @Size(max = 150, message = "Name must not exceed 150 characters") String name,
            @RequestParam(required = false) @Size(max = 100, message = "Email must not exceed 100 characters") String email,
            @RequestParam(required = false) @Size(max = 50, message = "City must not exceed 50 characters") String city,
            @RequestParam(required = false) @Size(max = 3, message = "Country must not exceed 3 characters") String country,
            @RequestParam(required = false) SupplierStatus status,
            @RequestParam(required = false) SupplierType supplierType,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxRating,
            @RequestParam(required = false) Integer minDeliveryDays,
            @RequestParam(required = false) Integer maxDeliveryDays) {

        Page<SupplierResponse> suppliers = supplierService.searchSuppliers(
                name, email, city, country,
                status, supplierType, minRating, maxRating,
                minDeliveryDays, maxDeliveryDays, pageable
        );

        return ResponseEntity.ok(suppliers);
    }
}