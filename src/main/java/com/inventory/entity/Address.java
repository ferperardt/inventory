package com.inventory.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address information")
public class Address {
    
    @Schema(description = "Street address", example = "123 Industrial Ave")
    @Column(name = "street_address", length = 200)
    private String streetAddress;
    
    @Schema(description = "City name", example = "San Francisco")
    @Column(length = 50)
    private String city;
    
    @Schema(description = "State or province", example = "CA")
    @Column(name = "state_province", length = 50)
    private String stateProvince;
    
    @Schema(description = "Postal or ZIP code", example = "94107")
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Schema(description = "Country code (ISO)", example = "USA")
    @Column(length = 3)
    private String country;
}