package com.inventory.controller;

import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.response.StockMovementResponse;
import com.inventory.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stock-movements")
@Tag(name = "Stock Movements", description = "Stock movement tracking and audit operations")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @Operation(
            summary = "Create stock movement",
            description = "Records a new stock movement (IN/OUT) for a product with reason and quantity tracking"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stock movement created successfully",
                    content = @Content(schema = @Schema(implementation = StockMovementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid movement data or insufficient stock",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<StockMovementResponse> createStockMovement(
            @Parameter(description = "Stock movement data including product, type, quantity and reason", required = true)
            @Valid @RequestBody CreateStockMovementRequest request) {
        StockMovementResponse response = stockMovementService.createStockMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all stock movements",
            description = "Retrieves a paginated list of all stock movements ordered by creation date (most recent first)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<StockMovementResponse>> getAllMovements(
            @Parameter(description = "Pagination parameters (default: 20 per page, sorted by createdAt)")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<StockMovementResponse> movements = stockMovementService.getAllMovements(pageable);
        return ResponseEntity.ok(movements);
    }
}