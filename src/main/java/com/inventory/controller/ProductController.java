package com.inventory.controller;

import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.UpdateProductRequest;
import com.inventory.dto.request.UpdateProductSuppliersRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.dto.response.StockMovementResponse;
import com.inventory.service.ProductService;
import com.inventory.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Validated
@Tag(name = "Products", description = "Product management operations")
public class ProductController {

    private final ProductService productService;
    private final StockMovementService stockMovementService;

    public ProductController(ProductService productService, StockMovementService stockMovementService) {
        this.productService = productService;
        this.stockMovementService = stockMovementService;
    }

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product in the inventory system with complete validation"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully", 
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product data", 
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "SKU already exists", 
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Product data to be created", required = true)
            @Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieves a paginated list of all products in the inventory"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a specific product by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product unique identifier", required = true)
            @PathVariable UUID id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(
            @PathVariable @NotBlank @Size(max = 50, message = "SKU must not exceed 50 characters") String sku) {
        ProductResponse product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Advanced product search",
            description = "Searches products using multiple filters including text fields, price ranges, and stock levels"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @Parameter(description = "Filter by product name (partial match)")
            @RequestParam(required = false) @Size(max = 100, message = "Name must not exceed 100 characters") String name,
            @Parameter(description = "Filter by category (partial match)")
            @RequestParam(required = false) @Size(max = 50, message = "Category must not exceed 50 characters") String category,
            @Parameter(description = "Filter by SKU (partial match)")
            @RequestParam(required = false) @Size(max = 50, message = "SKU must not exceed 50 characters") String sku,
            @Parameter(description = "Filter by description (partial match)")
            @RequestParam(required = false) @Size(max = 500, message = "Description must not exceed 500 characters") String description,
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Minimum stock quantity filter")
            @RequestParam(required = false) Integer minStock,
            @Parameter(description = "Maximum stock quantity filter")
            @RequestParam(required = false) Integer maxStock,
            @Parameter(description = "Filter products with stock below minimum level")
            @RequestParam(required = false) Boolean lowStock) {

        Page<ProductResponse> products = productService.searchProducts(
                name, category, sku, description,
                minPrice, maxPrice, minStock, maxStock,
                lowStock, pageable
        );

        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}/suppliers")
    public ResponseEntity<ProductResponse> updateProductSuppliers(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductSuppliersRequest request) {
        ProductResponse response = productService.updateProductSuppliers(id, request.supplierIds());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/stock-movements")
    public ResponseEntity<Page<StockMovementResponse>> getProductStockMovements(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<StockMovementResponse> movements = stockMovementService.getMovementsByProductId(id, pageable);
        return ResponseEntity.ok(movements);
    }
}