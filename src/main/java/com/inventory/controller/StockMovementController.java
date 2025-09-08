package com.inventory.controller;

import com.inventory.dto.response.StockMovementResponse;
import com.inventory.service.StockMovementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    public ResponseEntity<Page<StockMovementResponse>> getAllMovements(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<StockMovementResponse> movements = stockMovementService.getAllMovements(pageable);
        return ResponseEntity.ok(movements);
    }
}