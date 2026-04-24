package com.omeralkan.applicationmicroservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductServiceClient {

    @GetMapping("/api/v1/product-amounts/product/{productId}/active")
    com.omeralkan.applicationmicroservice.client.ProductAmountResponseClientDto getActiveAmountByProductId(@PathVariable Long productId);
}