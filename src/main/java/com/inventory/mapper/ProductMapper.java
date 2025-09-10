package com.inventory.mapper;

import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.UpdateProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {SupplierMapper.class})
public interface ProductMapper {
    
    @Mapping(target = "sku", expression = "java(product.getOriginalSku() != null ? product.getOriginalSku() : product.getSku())")
    @Mapping(target = "lowStock", expression = "java(product.isLowStock())")
    ProductResponse toResponse(Product product);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "originalSku", ignore = true)
    @Mapping(target = "suppliers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Product toEntity(CreateProductRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "originalSku", ignore = true)
    @Mapping(target = "suppliers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateProductFromRequest(UpdateProductRequest request, @MappingTarget Product product);
}