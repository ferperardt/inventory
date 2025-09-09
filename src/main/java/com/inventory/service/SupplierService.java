package com.inventory.service;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.request.UpdateSupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Supplier;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import com.inventory.exception.DuplicateBusinessIdException;
import com.inventory.exception.SupplierNotFoundException;
import com.inventory.mapper.SupplierMapper;
import com.inventory.repository.SupplierRepository;
import com.inventory.specification.SupplierSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

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

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .filter(Supplier::getActive)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        return supplierMapper.toResponse(supplier);
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

    @Transactional
    public SupplierResponse updateSupplier(UUID id, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .filter(Supplier::getActive)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        if (request.businessId() != null && !request.businessId().trim().isEmpty()) {
            if (supplierRepository.existsByBusinessIdAndActiveTrue(request.businessId()) &&
                !request.businessId().equals(supplier.getBusinessId())) {
                throw new DuplicateBusinessIdException(request.businessId());
            }
        }

        supplierMapper.updateEntity(request, supplier);
        Supplier updatedSupplier = supplierRepository.save(supplier);

        return supplierMapper.toResponse(updatedSupplier);
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> searchSuppliers(
            String name,
            String email,
            String city,
            String country,
            SupplierStatus status,
            SupplierType supplierType,
            BigDecimal minRating,
            BigDecimal maxRating,
            Integer minDeliveryDays,
            Integer maxDeliveryDays,
            Pageable pageable) {

        Specification<Supplier> spec = Specification.where(SupplierSpecification.isActive());

        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and(SupplierSpecification.hasName(name));
        }

        if (email != null && !email.trim().isEmpty()) {
            spec = spec.and(SupplierSpecification.hasEmail(email));
        }

        if (city != null && !city.trim().isEmpty()) {
            spec = spec.and(SupplierSpecification.hasCity(city));
        }

        if (country != null && !country.trim().isEmpty()) {
            spec = spec.and(SupplierSpecification.hasCountry(country));
        }

        if (status != null) {
            spec = spec.and(SupplierSpecification.hasStatus(status));
        }

        if (supplierType != null) {
            spec = spec.and(SupplierSpecification.hasSupplierType(supplierType));
        }

        if (minRating != null || maxRating != null) {
            spec = spec.and(SupplierSpecification.hasRatingBetween(minRating, maxRating));
        }

        if (minDeliveryDays != null || maxDeliveryDays != null) {
            spec = spec.and(SupplierSpecification.hasDeliveryDaysBetween(minDeliveryDays, maxDeliveryDays));
        }

        return supplierRepository.findAll(spec, pageable)
                .map(supplierMapper::toResponse);
    }
}