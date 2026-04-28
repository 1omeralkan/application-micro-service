package com.omeralkan.applicationmicroservice.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentTypeResponseClientDto {

    private Long id;
    private String code;
    private String name;
    private Integer minInstallment;
    private Integer maxInstallment;
    private Boolean isActive;
}