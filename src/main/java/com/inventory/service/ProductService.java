package com.inventory.service;

import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.request.UpdateProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import com.inventory.exception.*;
import com.inventory.mapper.ProductMapper;
import com.inventory.repository.ProductRepository;
import com.inventory.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final StockMovementService stockMovementService;
    private final SupplierService supplierService;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, StockMovementService stockMovementService, SupplierService supplierService) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.stockMovementService = stockMovementService;
        this.supplierService = supplierService;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        // Validate SKU uniqueness
        if (productRepository.existsBySkuAndActiveTrue(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }

        // Validate business rule: stock quantity should not be below minimum level
        validateStockLevel(request.stockQuantity(), request.minStockLevel());

        // Validate suppliers exist
        List<Supplier> suppliers = request.supplierIds().stream()
                .map(supplierId -> {
                    try {
                        return supplierService.getSupplierEntityById(supplierId);
                    } catch (Exception e) {
                        throw new SupplierNotFoundException(supplierId);
                    }
                })
                .collect(Collectors.toList());

        Product product = productMapper.toEntity(request);
        product.setSuppliers(suppliers);

        Product savedProduct = productRepository.save(product);

        Integer stockQuantity = request.stockQuantity() != null ? request.stockQuantity() : 0;

        CreateStockMovementRequest movementRequest = new CreateStockMovementRequest(
                savedProduct.getId(),
                MovementType.IN,
                stockQuantity,
                MovementReason.INITIAL_STOCK,
                "Initial stock on product creation",
                "Initial stock set during product creation"
        );
        stockMovementService.createStockMovement(movementRequest);


        return productMapper.toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySkuAndActiveTrue(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));

        return productMapper.toResponse(product);
    }


    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        // Find the product by ID and ensure it's active
        Product product = productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Check if SKU is being changed and if new SKU already exists
        if (!product.getSku().equals(request.sku())) {
            // If changing SKU, check if new SKU already exists among active products
            if (productRepository.existsBySkuAndActiveTrue(request.sku())) {
                throw new DuplicateSkuException(request.sku());
            }
        }

        // Update the product using MapStruct (stockQuantity is no longer in UpdateProductRequest)
        productMapper.updateProductFromRequest(request, product);

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Validate that product has no stock before deletion
        Integer currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        if (currentStock > 0) {
            throw new ProductHasStockException(product.getSku(), currentStock);
        }

        product.softDelete();
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String name,
            String category,
            String sku,
            String description,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minStock,
            Integer maxStock,
            Boolean lowStock,
            Pageable pageable) {

        Specification<Product> spec = ProductSpecification.isActive();

        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and(ProductSpecification.hasName(name));
        }

        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and(ProductSpecification.hasCategory(category));
        }

        if (sku != null && !sku.trim().isEmpty()) {
            spec = spec.and(ProductSpecification.hasSku(sku));
        }

        if (description != null && !description.trim().isEmpty()) {
            spec = spec.and(ProductSpecification.hasDescription(description));
        }

        if (minPrice != null || maxPrice != null) {
            spec = spec.and(ProductSpecification.hasPriceBetween(minPrice, maxPrice));
        }

        if (minStock != null || maxStock != null) {
            spec = spec.and(ProductSpecification.hasStockQuantityBetween(minStock, maxStock));
        }

        if (Boolean.TRUE.equals(lowStock)) {
            spec = spec.and(ProductSpecification.isLowStock());
        }

        return productRepository.findAll(spec, pageable)
                .map(productMapper::toResponse);
    }

    private void validateStockLevel(Integer stockQuantity, Integer minStockLevel) {
        if (stockQuantity != null && minStockLevel != null && stockQuantity < minStockLevel) {
            throw new InvalidStockLevelException(
                    "Stock quantity (" + stockQuantity + ") cannot be below minimum stock level (" + minStockLevel + ")"
            );
        }
    }
}