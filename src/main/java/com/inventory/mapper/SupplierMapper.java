package com.inventory.mapper;

import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    
    SupplierResponse toResponse(Supplier supplier);
}