package com.inventory.controller;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.request.UpdateSupplierRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import com.inventory.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Tag(name = "Suppliers", description = "Supplier management operations and relationships")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Operation(
            summary = "Create a new supplier",
            description = "Creates a new supplier with contact information and business details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supplier created successfully",
                    content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid supplier data",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Business ID or email already exists",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @Parameter(description = "Supplier data to be created", required = true)
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all suppliers",
            description = "Retrieves a paginated list of all suppliers"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SupplierResponse> suppliers = supplierService.getAllSuppliers(pageable);
        return ResponseEntity.ok(suppliers);
    }

    @Operation(
            summary = "Get supplier by ID",
            description = "Retrieves a specific supplier by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier found",
                    content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @Parameter(description = "Supplier unique identifier", required = true, example = "456e1234-e89b-12d3-a456-426614174001")
            @PathVariable UUID id) {
        SupplierResponse supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @Operation(
            summary = "Update supplier",
            description = "Updates an existing supplier with new information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier updated successfully",
                    content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid supplier data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @Parameter(description = "Supplier unique identifier", required = true, example = "456e1234-e89b-12d3-a456-426614174001")
            @PathVariable UUID id,
            @Parameter(description = "Updated supplier data", required = true)
            @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse supplier = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(supplier);
    }

    @Operation(
            summary = "Advanced supplier search",
            description = "Searches suppliers using filters including business details, location, ratings, and delivery performance"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<SupplierResponse>> searchSuppliers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by supplier name (partial match)")
            @RequestParam(required = false) @Size(max = 150, message = "Name must not exceed 150 characters") String name,
            @Parameter(description = "Filter by email address (partial match)")
            @RequestParam(required = false) @Size(max = 100, message = "Email must not exceed 100 characters") String email,
            @Parameter(description = "Filter by city location")
            @RequestParam(required = false) @Size(max = 50, message = "City must not exceed 50 characters") String city,
            @Parameter(description = "Filter by country (ISO code)")
            @RequestParam(required = false) @Size(max = 3, message = "Country must not exceed 3 characters") String country,
            @Parameter(description = "Filter by supplier status (ACTIVE, INACTIVE, SUSPENDED)")
            @RequestParam(required = false) SupplierStatus status,
            @Parameter(description = "Filter by supplier type (DOMESTIC, INTERNATIONAL)")
            @RequestParam(required = false) SupplierType supplierType,
            @Parameter(description = "Minimum rating filter (1.0 to 5.0)")
            @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "Maximum rating filter (1.0 to 5.0)")
            @RequestParam(required = false) BigDecimal maxRating,
            @Parameter(description = "Minimum delivery days filter")
            @RequestParam(required = false) Integer minDeliveryDays,
            @Parameter(description = "Maximum delivery days filter")
            @RequestParam(required = false) Integer maxDeliveryDays) {

        Page<SupplierResponse> suppliers = supplierService.searchSuppliers(
                name, email, city, country,
                status, supplierType, minRating, maxRating,
                minDeliveryDays, maxDeliveryDays, pageable
        );

        return ResponseEntity.ok(suppliers);
    }

    @Operation(
            summary = "Get supplier's products",
            description = "Retrieves all products associated with a specific supplier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content)
    })
    @GetMapping("/{id}/products")
    public ResponseEntity<Page<ProductResponse>> getSupplierProducts(
            @Parameter(description = "Supplier unique identifier", required = true, example = "456e1234-e89b-12d3-a456-426614174001")
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponse> products = supplierService.getSupplierProducts(id, pageable);
        return ResponseEntity.ok(products);
    }
}