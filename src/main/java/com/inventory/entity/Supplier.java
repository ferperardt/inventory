package com.inventory.entity;

import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Supplier extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;
    
    @Column(nullable = false, length = 150)
    @NotNull
    private String name;
    
    @Column(name = "business_id", unique = true, length = 50)
    private String businessId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierStatus status = SupplierStatus.ACTIVE;
    
    @Email
    @Column(length = 100, nullable = false)
    @NotNull
    private String email;
    
    @Column(length = 20, nullable = false)
    @NotNull
    private String phone;
    
    @Column(name = "contact_person", length = 100)
    private String contactPerson;
    
    @Embedded
    private Address address;
    
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;
    
    @Column(name = "avg_delivery_days")
    private Integer averageDeliveryDays;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "supplier_type")
    private SupplierType supplierType;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(precision = 3, scale = 2)
    @DecimalMin("1.0")
    @DecimalMax("5.0")
    private BigDecimal rating;
    
    @ManyToMany(mappedBy = "suppliers", fetch = FetchType.LAZY)
    private List<Product> products;
}