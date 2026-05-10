package com.omeralkan.applicationmicroservice.service.impl;

import com.omeralkan.applicationmicroservice.client.*;
import com.omeralkan.applicationmicroservice.dto.request.ApplicationRequestDto;
import com.omeralkan.applicationmicroservice.dto.response.ApplicationResponseDto;
import com.omeralkan.applicationmicroservice.entity.ApplicationCoverageEntity;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
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
    private final ParameterServiceClient parameterServiceClient;
    private final CollectionServiceClient collectionServiceClient;


    @Override
    @Transactional
    public ApplicationResponseDto createApplication(ApplicationRequestDto requestDto) {
        CustomerResponseClientDto customer = getCustomerOrThrow(requestDto.getCustomerId());
        ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(requestDto.getProductId());
        ProductResponseClientDto product = getProductOrThrow(requestDto.getProductId());
        PaymentTypeResponseClientDto paymentType = getPaymentTypeOrThrow(requestDto.getPaymentTypeCode());
        validateInstallmentCount(requestDto.getInstallmentCount(), paymentType);
        String applicationNumber = generateApplicationNumber();

        ApplicationEntity entity = applicationMapper.toEntity(requestDto, productAmount.getId(), applicationNumber);

        List<ApplicationCoverageEntity> coverages = calculateCoverages(product, productAmount.getAmount(), entity);
        entity.setCoverages(coverages);
        ApplicationEntity savedEntity = applicationRepository.save(entity);

        log.info("Başvuru oluşturuldu. No: {}, Müşteri: {} {}, Ürün: {}, Fiyat: {}, Ödeme: {}, Taksit: {}",
                applicationNumber,
                customer.getAd(), customer.getSoyad(),
                productAmount.getProductName(),
                productAmount.getAmount(),
                requestDto.getPaymentTypeCode(),
                requestDto.getInstallmentCount());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                createCollectionsForApplication(savedEntity.getId());
            }
        });

        return applicationMapper.toResponse(savedEntity, customer, productAmount, paymentType);
    }

    @Override
    public ApplicationResponseDto getApplicationById(Long id) {
        ApplicationEntity entity = findActiveApplicationOrThrow(id);
        CustomerResponseClientDto customer = getCustomerOrThrow(entity.getCustomerId());
        ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(entity.getProductId());
        PaymentTypeResponseClientDto paymentType = getPaymentTypeOrThrow(entity.getPaymentTypeCode());
        return applicationMapper.toResponse(entity, customer, productAmount, paymentType);
    }

    @Override
    public ApplicationResponseDto getApplicationByNumber(String applicationNumber) {
        ApplicationEntity entity = applicationRepository
                .findByApplicationNumberAndIsActiveTrue(applicationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCodes.APPLICATION_NOT_FOUND, HttpStatus.NOT_FOUND));
        CustomerResponseClientDto customer = getCustomerOrThrow(entity.getCustomerId());
        ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(entity.getProductId());
        PaymentTypeResponseClientDto paymentType = getPaymentTypeOrThrow(entity.getPaymentTypeCode());
        return applicationMapper.toResponse(entity, customer, productAmount, paymentType);
    }

    @Override
    public List<ApplicationResponseDto> getAllApplications() {
        return applicationRepository.findAllByIsActiveTrue()
                .stream()
                .map(entity -> {
                    CustomerResponseClientDto customer = getCustomerOrThrow(entity.getCustomerId());
                    ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(entity.getProductId());
                    PaymentTypeResponseClientDto paymentType = getPaymentTypeOrThrow(entity.getPaymentTypeCode());
                    return applicationMapper.toResponse(entity, customer, productAmount, paymentType);
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
                    PaymentTypeResponseClientDto paymentType = getPaymentTypeOrThrow(entity.getPaymentTypeCode());
                    return applicationMapper.toResponse(entity, customer, productAmount, paymentType);
                })
                .toList();
    }

    @Override
    public ApplicationResponseDto updateApplicationStatus(Long id, String status) {
        ApplicationEntity entity = findActiveApplicationOrThrow(id);
        ApplicationStatus newStatus = ApplicationStatus.valueOf(status.toUpperCase());
        entity.setStatus(newStatus);
        ApplicationEntity updatedEntity = applicationRepository.save(entity);
        log.info("Başvuru durumu güncellendi. No: {}, Yeni Durum: {}", entity.getApplicationNumber(), newStatus);
        CustomerResponseClientDto customer = getCustomerOrThrow(entity.getCustomerId());
        ProductAmountResponseClientDto productAmount = getActiveAmountOrThrow(entity.getProductId());
        PaymentTypeResponseClientDto paymentType = getPaymentTypeOrThrow(entity.getPaymentTypeCode());
        return applicationMapper.toResponse(updatedEntity, customer, productAmount, paymentType);
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
                .orElseThrow(() -> new BusinessException(ErrorCodes.APPLICATION_NOT_FOUND, HttpStatus.NOT_FOUND));
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

    private PaymentTypeResponseClientDto getPaymentTypeOrThrow(String paymentTypeCode) {
        try {
            return parameterServiceClient.getPaymentTypeByCode(paymentTypeCode);
        } catch (Exception e) {
            log.error("Parameter servisi hatası. PaymentTypeCode: {}, Hata: {}", paymentTypeCode, e.getMessage());
            throw new BusinessException(ErrorCodes.PARAMETER_SERVICE_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void validateInstallmentCount(Integer installmentCount, PaymentTypeResponseClientDto paymentType) {
        if (installmentCount < paymentType.getMinInstallment()
                || installmentCount > paymentType.getMaxInstallment()) {
            log.error("Geçersiz taksit sayısı. Girilen: {}, Min: {}, Max: {}",
                    installmentCount, paymentType.getMinInstallment(), paymentType.getMaxInstallment());
            throw new BusinessException(ErrorCodes.INVALID_INSTALLMENT_COUNT, HttpStatus.BAD_REQUEST);
        }
    }

    private String generateApplicationNumber() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = applicationRepository.count() + 1;
        return String.format("APP-%s-%04d", year, count);
    }

    private void createCollectionsForApplication(Long applicationId) {
        try {
            collectionServiceClient.createCollections(applicationId);
            log.info("Tahsilat oluşturuldu. ApplicationId: {}", applicationId);
        } catch (Exception e) {
            log.error("Collection servisi hatası. ApplicationId: {}, Hata: {}",
                    applicationId, e.getMessage());
            throw new BusinessException(
                    ErrorCodes.COLLECTION_SERVICE_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private ProductResponseClientDto getProductOrThrow(Long productId) {
        try {
            return productServiceClient.getProductById(productId);
        } catch (Exception e) {
            log.error("Product servisi hatası (Ürün detayı). ProductId: {}, Hata: {}", productId, e.getMessage());
            throw new BusinessException(ErrorCodes.PRODUCT_SERVICE_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private List<ApplicationCoverageEntity> calculateCoverages(ProductResponseClientDto product, BigDecimal premiumAmount, ApplicationEntity application) {
        List<ApplicationCoverageEntity> applicationCoverages = new java.util.ArrayList<>();

        if (product.getCoverages() == null || product.getCoverages().isEmpty()) {
            return applicationCoverages;
        }

        for (ProductCoverageResponseClientDto catalogCoverage : product.getCoverages()) {
            BigDecimal calculatedAmount = BigDecimal.ZERO;

            if ("VEFAT".equalsIgnoreCase(catalogCoverage.getCoverageCode())) {
                calculatedAmount = premiumAmount.multiply(BigDecimal.valueOf(100));
            } else if ("MALULIYET".equalsIgnoreCase(catalogCoverage.getCoverageCode())) {
                calculatedAmount = premiumAmount.multiply(BigDecimal.valueOf(50));
            } else {
                calculatedAmount = premiumAmount.multiply(BigDecimal.valueOf(10));
            }

            if (calculatedAmount.compareTo(catalogCoverage.getMaxAmount()) > 0) {
                calculatedAmount = catalogCoverage.getMaxAmount();
            } else if (calculatedAmount.compareTo(catalogCoverage.getMinAmount()) < 0) {
                calculatedAmount = catalogCoverage.getMinAmount();
            }

            ApplicationCoverageEntity coverageEntity = new ApplicationCoverageEntity();
            coverageEntity.setApplication(application);
            coverageEntity.setCoverageCode(catalogCoverage.getCoverageCode());
            coverageEntity.setName(catalogCoverage.getName());
            coverageEntity.setCalculatedAmount(calculatedAmount);

            applicationCoverages.add(coverageEntity);
        }

        return applicationCoverages;
    }
}