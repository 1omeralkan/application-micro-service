package com.omeralkan.applicationmicroservice.service.impl;

import com.omeralkan.applicationmicroservice.client.*;
import com.omeralkan.applicationmicroservice.dto.request.ApplicationRequestDto;
import com.omeralkan.applicationmicroservice.dto.response.ApplicationResponseDto;
import com.omeralkan.applicationmicroservice.dto.response.RiskCalculationResult;
import com.omeralkan.applicationmicroservice.entity.ApplicationCoverageEntity;
import com.omeralkan.applicationmicroservice.entity.ApplicationEntity;
import com.omeralkan.applicationmicroservice.entity.ApplicationRiskProfileEntity;
import com.omeralkan.applicationmicroservice.entity.ApplicationStatus;
import com.omeralkan.applicationmicroservice.exception.BusinessException;
import com.omeralkan.applicationmicroservice.exception.ErrorCodes;
import com.omeralkan.applicationmicroservice.mapper.ApplicationMapper;
import com.omeralkan.applicationmicroservice.repository.ApplicationRepository;
import com.omeralkan.applicationmicroservice.service.ApplicationService;
import com.omeralkan.applicationmicroservice.service.RiskEngineService;
import com.omeralkan.applicationmicroservice.util.CoverageCalculatorUtil;
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
    private final RiskEngineService riskEngineService;

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

        RiskCalculationResult riskResult = riskEngineService.calculateRisk(
                productAmount,
                requestDto.getAge(),
                requestDto.getHeight(),
                requestDto.getWeight(),
                requestDto.getGender()
        );

        entity.setIsHealthReportRequired(riskResult.isReportRequired());

        ApplicationRiskProfileEntity riskProfile = new ApplicationRiskProfileEntity();
        riskProfile.setApplication(entity);
        riskProfile.setAge(requestDto.getAge());
        riskProfile.setHeight(requestDto.getHeight());
        riskProfile.setWeight(requestDto.getWeight());
        riskProfile.setGender(requestDto.getGender());
        entity.setRiskProfile(riskProfile);


        List<ApplicationCoverageEntity> coverages = new java.util.ArrayList<>();

        if (product.getCoverages() != null && requestDto.getRequestedCoverageCodes() != null) {
            for (ProductCoverageResponseClientDto catalogCoverage : product.getCoverages()) {

                if (requestDto.getRequestedCoverageCodes().contains(catalogCoverage.getCoverageCode())) {

                    BigDecimal multiplier = parameterServiceClient.getCoverageMultiplierByCode(catalogCoverage.getCoverageCode());

                    ApplicationCoverageEntity coverage = CoverageCalculatorUtil.createCoverageEntity(
                            catalogCoverage, riskResult.finalPremium(), entity, multiplier);

                    coverages.add(coverage);
                }
            }
        }
        entity.setCoverages(coverages);


        ApplicationEntity savedEntity = applicationRepository.save(entity);

        log.info("Başvuru oluşturuldu. No: {}, Baz Fiyat: {}, Riskli Fiyat: {}, Rapor Lazım Mı: {}",
                applicationNumber,
                productAmount.getAmount(),
                riskResult.finalPremium(),
                riskResult.isReportRequired());

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


}