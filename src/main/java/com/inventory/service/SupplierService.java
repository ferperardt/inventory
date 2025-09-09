package com.inventory.service;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Supplier;
import com.inventory.exception.DuplicateBusinessIdException;
import com.inventory.mapper.SupplierMapper;
import com.inventory.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findByActiveTrue(pageable)
                .map(supplierMapper::toResponse);
    }

    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        if (request.businessId() != null && !request.businessId().trim().isEmpty()) {
            if (supplierRepository.existsByBusinessIdAndActiveTrue(request.businessId())) {
                throw new DuplicateBusinessIdException(request.businessId());
            }
        }

        Supplier supplier = supplierMapper.toEntity(request);
        Supplier savedSupplier = supplierRepository.save(supplier);

        return supplierMapper.toResponse(savedSupplier);
    }
}