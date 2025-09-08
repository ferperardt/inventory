package com.inventory.controller;

import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.response.StockMovementResponse;
import com.inventory.service.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping
    public ResponseEntity<StockMovementResponse> createStockMovement(@Valid @RequestBody CreateStockMovementRequest request) {
        StockMovementResponse response = stockMovementService.createStockMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<StockMovementResponse>> getAllMovements(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<StockMovementResponse> movements = stockMovementService.getAllMovements(pageable);
        return ResponseEntity.ok(movements);
    }
}