package com.omeralkan.applicationmicroservice.client;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CollectionResponseClientDto {

    private Long id;
    private Long applicationId;
    private Long policyId;
    private Integer installmentNumber;
    private BigDecimal installmentAmount;
    private LocalDate dueDate;
    private String status;
    private Boolean isActive;
}