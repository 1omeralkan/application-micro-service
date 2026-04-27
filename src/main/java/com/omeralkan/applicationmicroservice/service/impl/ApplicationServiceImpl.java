package com.omeralkan.applicationmicroservice.service.impl;

import com.omeralkan.applicationmicroservice.client.CustomerResponseClientDto;
import com.omeralkan.applicationmicroservice.client.CustomerServiceClient;
import com.omeralkan.applicationmicroservice.client.ProductAmountResponseClientDto;
import com.omeralkan.applicationmicroservice.client.ProductServiceClient;
import com.omeralkan.applicationmicroservice.dto.request.ApplicationRequestDto;
import com.omeralkan.applicationmicroservice.dto.response.ApplicationResponseDto;
import com.omeralkan.applicationmicroservice.entity.ApplicationEntity;
import com.omeralkan.applicationmicroservice.entity.ApplicationStatus;
import com.omeralkan.applicationmicroservice.exception.BusinessException;
import com.omeralkan.applicationmicroservice.exception.ErrorCodes;
import com.omeralkan.applicationmicroservice.mapper.ApplicationMapper;
import com.omeralkan.applicationmicroservice.repository.ApplicationRepository;
import com.omeralkan.applicationmicroservice.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final CustomerServiceClient customerServiceClient;
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional
    public ApplicationResponseDto createApplication(ApplicationRequestDto requestDto) {

        CustomerResponseClientDto customer = getCustomerOrThrow(requestDto.getCustomerId());

        ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(requestDto.getProductId());

        String applicationNumber = generateApplicationNumber();

        ApplicationEntity entity = applicationMapper.toEntity(
                requestDto, productAmount.getId(), applicationNumber);

        ApplicationEntity savedEntity = applicationRepository.save(entity);

        log.info("Başvuru oluşturuldu. No: {}, Müşteri: {} {}, Ürün: {}, Fiyat: {}",
                applicationNumber,
                customer.getAd(), customer.getSoyad(),
                productAmount.getProductName(),
                productAmount.getAmount());

        return applicationMapper.toResponse(savedEntity, customer, productAmount);
    }

    @Override
    public ApplicationResponseDto getApplicationById(Long id) {
        ApplicationEntity entity = findActiveApplicationOrThrow(id);

        CustomerResponseClientDto customer = getCustomerOrThrow(entity.getCustomerId());
        ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(entity.getProductId());

        return applicationMapper.toResponse(entity, customer, productAmount);
    }


    @Override
    public ApplicationResponseDto getApplicationByNumber(String applicationNumber) {
        ApplicationEntity entity = applicationRepository
                .findByApplicationNumberAndIsActiveTrue(applicationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCodes.APPLICATION_NOT_FOUND, HttpStatus.NOT_FOUND));

        CustomerResponseClientDto customer = getCustomerOrThrow(entity.getCustomerId());
        ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(entity.getProductId());

        return applicationMapper.toResponse(entity, customer, productAmount);
    }


    @Override
    public List<ApplicationResponseDto> getAllApplications() {
        return applicationRepository.findAllByIsActiveTrue()
                .stream()
                .map(entity -> {
                    CustomerResponseClientDto customer = getCustomerOrThrow(entity.getCustomerId());
                    ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(entity.getProductId());
                    return applicationMapper.toResponse(entity, customer, productAmount);
                })
                .toList();
    }


    @Override
    public List<ApplicationResponseDto> getApplicationsByCustomerId(Long customerId) {
        getCustomerOrThrow(customerId);

        return applicationRepository.findAllByCustomerIdAndIsActiveTrue(customerId)
                .stream()
                .map(entity -> {
                    CustomerResponseClientDto customer = getCustomerOrThrow(entity.getCustomerId());
                    ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(entity.getProductId());
                    return applicationMapper.toResponse(entity, customer, productAmount);
                })
                .toList();
    }


    @Override
    public ApplicationResponseDto updateApplicationStatus(Long id, String status) {
        ApplicationEntity entity = findActiveApplicationOrThrow(id);

        ApplicationStatus newStatus = ApplicationStatus.valueOf(status.toUpperCase());
        entity.setStatus(newStatus);
        ApplicationEntity updatedEntity = applicationRepository.save(entity);

        log.info("Başvuru durumu güncellendi. No: {}, Yeni Durum: {}",
                entity.getApplicationNumber(), newStatus);

        CustomerResponseClientDto customer = getCustomerOrThrow(entity.getCustomerId());
        ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(entity.getProductId());

        return applicationMapper.toResponse(updatedEntity, customer, productAmount);
    }

    @Override
    public void deleteApplication(Long id) {
        ApplicationEntity entity = findActiveApplicationOrThrow(id);
        entity.setIsActive(false);
        applicationRepository.save(entity);

        log.info("Başvuru silindi. No: {}", entity.getApplicationNumber());
    }

    private ApplicationEntity findActiveApplicationOrThrow(Long id) {
        return applicationRepository.findById(id)
                .filter(ApplicationEntity::getIsActive)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.APPLICATION_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private CustomerResponseClientDto getCustomerOrThrow(Long customerId) {
        try {
            return customerServiceClient.getCustomerById(customerId);
        } catch (Exception e) {
            log.error("Customer servisi hatası. CustomerId: {}, Hata: {}", customerId, e.getMessage());
            throw new BusinessException(ErrorCodes.CUSTOMER_SERVICE_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private ProductAmountResponseClientDto getActiveAmountOrThrow(Long productId) {
        try {
            return productServiceClient.getActiveAmountByProductId(productId);
        } catch (Exception e) {
            log.error("Product servisi hatası. ProductId: {}, Hata: {}", productId, e.getMessage());
            throw new BusinessException(ErrorCodes.PRODUCT_SERVICE_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String generateApplicationNumber() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = applicationRepository.count() + 1;
        return String.format("APP-%s-%04d", year, count);
    }
}