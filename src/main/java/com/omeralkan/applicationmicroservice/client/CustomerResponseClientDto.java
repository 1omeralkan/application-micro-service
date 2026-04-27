package com.omeralkan.applicationmicroservice.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponseClientDto {

    private Long id;
    private String ad;
    private String soyad;
    private Boolean isActive;
}