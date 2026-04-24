package com.omeralkan.applicationmicroservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestDto {

    private Long customerId;
    private Long productId;
    private String description;

}