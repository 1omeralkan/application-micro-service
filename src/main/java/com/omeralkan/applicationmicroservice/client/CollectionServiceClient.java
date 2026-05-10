package com.omeralkan.applicationmicroservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "collection-service", url = "${collection.service.url}")
public interface CollectionServiceClient {

    @PostMapping("/api/v1/collections/application/{applicationId}")
    List<CollectionResponseClientDto> createCollections(@PathVariable Long applicationId);
}