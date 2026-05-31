package com.omeralkan.applicationmicroservice.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCoverageResponseClientDto {
    private Long id;
    private String coverageCode;
    private String name;

}