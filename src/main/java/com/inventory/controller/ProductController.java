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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
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
            @Parameter(description = "Product unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @Operation(
            summary = "Get product by SKU",
            description = "Retrieves a specific product by its Stock Keeping Unit (SKU)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content)
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(
            @Parameter(description = "Product SKU identifier", required = true, example = "WM-LOG-MX3-001")
            @PathVariable @NotBlank @Size(max = 50, message = "SKU must not exceed 50 characters") String sku) {
        ProductResponse product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @Operation(
            summary = "Update product",
            description = "Updates an existing product with new information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "SKU already exists",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(description = "Updated product data", required = true)
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete product",
            description = "Soft deletes a product (marks as inactive instead of permanent deletion)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Advanced product search",
            description = "Searches products using multiple filters including text fields, price ranges, and stock levels. " +
                    "Pagination: Use query parameters ?page=0&size=20&sort=name,asc (all optional)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by product name (partial match)", example = "mouse")
            @RequestParam(required = false) @Size(max = 100, message = "Name must not exceed 100 characters") String name,
            @Parameter(description = "Filter by category (partial match)", example = "electronics")
            @RequestParam(required = false) @Size(max = 50, message = "Category must not exceed 50 characters") String category,
            @Parameter(description = "Filter by SKU (partial match)", example = "WM-LOG")
            @RequestParam(required = false) @Size(max = 50, message = "SKU must not exceed 50 characters") String sku,
            @Parameter(description = "Filter by description (partial match)", example = "wireless")
            @RequestParam(required = false) @Size(max = 500, message = "Description must not exceed 500 characters") String description,
            @Parameter(description = "Minimum price filter", example = "50.00")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter", example = "200.00")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Minimum stock quantity filter", example = "10")
            @RequestParam(required = false) Integer minStock,
            @Parameter(description = "Maximum stock quantity filter", example = "100")
            @RequestParam(required = false) Integer maxStock,
            @Parameter(description = "Filter products with stock below minimum level", example = "true")
            @RequestParam(required = false) Boolean lowStock) {

        Page<ProductResponse> products = productService.searchProducts(
                name, category, sku, description,
                minPrice, maxPrice, minStock, maxStock,
                lowStock, pageable
        );

        return ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Update product suppliers",
            description = "Updates the list of suppliers associated with a product"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product suppliers updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid supplier data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content)
    })
    @PutMapping("/{id}/suppliers")
    public ResponseEntity<ProductResponse> updateProductSuppliers(
            @Parameter(description = "Product unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(description = "List of supplier IDs to associate", required = true)
            @Valid @RequestBody UpdateProductSuppliersRequest request) {
        ProductResponse response = productService.updateProductSuppliers(id, request.supplierIds());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get product stock movements",
            description = "Retrieves all stock movements (IN/OUT) for a specific product with pagination"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content)
    })
    @GetMapping("/{id}/stock-movements")
    public ResponseEntity<Page<StockMovementResponse>> getProductStockMovements(
            @Parameter(description = "Product unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StockMovementResponse> movements = stockMovementService.getMovementsByProductId(id, pageable);
        return ResponseEntity.ok(movements);
    }
}