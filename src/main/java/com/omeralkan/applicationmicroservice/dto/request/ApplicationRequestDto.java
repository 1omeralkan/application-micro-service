package com.omeralkan.applicationmicroservice.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ApplicationRequestDto {

    private Long customerId;
    private Long productId;
    private String description;
    private String paymentTypeCode;
    private Integer installmentCount;
    private List<String> requestedCoverageCodes;
    private Integer age;
    private Integer height;
    private BigDecimal weight;
    private String gender;
}