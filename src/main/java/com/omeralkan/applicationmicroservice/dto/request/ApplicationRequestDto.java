package com.omeralkan.applicationmicroservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestDto {

    private Long customerId;
    private Long productId;
    private String description;
    private String paymentTypeCode;   // PESIN veya TAKSITLI
    private Integer installmentCount; // Kaç taksit (peşinse 1, taksitliyse 2-12)
}