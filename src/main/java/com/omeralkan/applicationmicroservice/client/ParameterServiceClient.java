package com.omeralkan.applicationmicroservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "parameter-service", url = "${parameter.service.url}")
public interface ParameterServiceClient {

    @GetMapping("/api/v1/payment-types/code/{code}")
    PaymentTypeResponseClientDto getPaymentTypeByCode(@PathVariable String code);
}