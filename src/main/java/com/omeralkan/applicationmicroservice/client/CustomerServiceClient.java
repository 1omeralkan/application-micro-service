package com.omeralkan.applicationmicroservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "${customer.service.url}")
public interface CustomerServiceClient {

    @GetMapping("/api/v1/customers/{id}")
    com.omeralkan.applicationmicroservice.client.CustomerResponseClientDto getCustomerById(@PathVariable Long id);
}