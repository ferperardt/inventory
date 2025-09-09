package com.inventory.controller;

import com.inventory.dto.response.SupplierResponse;
import com.inventory.service.SupplierService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<SupplierResponse> suppliers = supplierService.getAllSuppliers(pageable);
        return ResponseEntity.ok(suppliers);
    }
}