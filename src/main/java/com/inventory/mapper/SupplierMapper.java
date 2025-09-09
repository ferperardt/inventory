package com.inventory.mapper;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    
    SupplierResponse toResponse(Supplier supplier);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Supplier toEntity(CreateSupplierRequest request);
}