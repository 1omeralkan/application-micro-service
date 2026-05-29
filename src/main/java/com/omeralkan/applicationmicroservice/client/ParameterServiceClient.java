package com.omeralkan.applicationmicroservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "parameter-service", url = "${parameter.service.url}")
public interface ParameterServiceClient {

    @GetMapping("/api/v1/payment-types/code/{code}")
    PaymentTypeResponseClientDto getPaymentTypeByCode(@PathVariable String code);

    @GetMapping("/api/v1/coverage-parameters/{code}/multiplier")
    BigDecimal getCoverageMultiplierByCode(@PathVariable("code") String code);

    @GetMapping("/api/v1/risk-parameters/{key}/value")
    BigDecimal getRiskParameterValue(@PathVariable("key") String key);

    @GetMapping("/api/v1/risk-parameters/all")
    Map<String, BigDecimal> getAllRiskParameters();
}