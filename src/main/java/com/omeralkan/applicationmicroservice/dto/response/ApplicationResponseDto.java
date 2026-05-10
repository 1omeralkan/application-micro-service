package com.omeralkan.applicationmicroservice.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ApplicationResponseDto {

    private Long id;
    private String applicationNumber;
    private Long customerId;
    private String customerName;
    private Long productId;
    private String productName;
    private Long productAmountId;
    private BigDecimal amount;
    private LocalDate applicationDate;
    private String status;
    private String description;
    private Boolean isActive;
    private String paymentTypeCode;
    private String paymentTypeName;
    private Integer installmentCount;
    private BigDecimal installmentAmount;
}