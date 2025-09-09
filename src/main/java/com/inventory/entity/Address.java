package com.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    @Column(name = "street_address", length = 200)
    private String streetAddress;
    
    @Column(length = 50)
    private String city;
    
    @Column(name = "state_province", length = 50)
    private String stateProvince;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(length = 3)
    private String country;
}