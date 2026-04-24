package com.omeralkan.applicationmicroservice.client;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductAmountResponseClientDto {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    private Boolean isActive;
}