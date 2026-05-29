package com.omeralkan.applicationmicroservice.client;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductCoverageResponseClientDto {
    private Long id;
    private String coverageCode;
    private String name;

}