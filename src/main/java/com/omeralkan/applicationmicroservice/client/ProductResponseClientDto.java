package com.omeralkan.applicationmicroservice.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductResponseClientDto {
    private Long id;
    private String productCode;
    private String name;
    private List<ProductCoverageResponseClientDto> coverages;
}