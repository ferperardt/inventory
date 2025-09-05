package com.inventory.service;

import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.UpdateProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import com.inventory.exception.DuplicateSkuException;
import com.inventory.exception.InvalidStockLevelException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.mapper.ProductMapper;
import com.inventory.repository.ProductRepository;
import com.inventory.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        // Validate SKU uniqueness
        if (productRepository.existsBySkuAndActiveTrue(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }

        // Validate business rule: stock quantity should not be below minimum level
        validateStockLevel(request.stockQuantity(), request.minStockLevel());

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
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

        // Validate business rule: stock quantity should not be below minimum level
        validateStockLevel(request.stockQuantity(), request.minStockLevel());

        // Update the product using MapStruct
        productMapper.updateProductFromRequest(request, product);
        
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> new ProductNotFoundException(id));

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

        Specification<Product> spec = Specification.where(ProductSpecification.isActive());

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