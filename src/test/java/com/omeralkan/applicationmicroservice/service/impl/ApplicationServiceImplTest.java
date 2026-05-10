package com.omeralkan.applicationmicroservice.service.impl;

import com.omeralkan.applicationmicroservice.client.*;
import com.omeralkan.applicationmicroservice.dto.request.ApplicationRequestDto;
import com.omeralkan.applicationmicroservice.dto.response.ApplicationResponseDto;
import com.omeralkan.applicationmicroservice.entity.ApplicationEntity;
import com.omeralkan.applicationmicroservice.entity.ApplicationStatus;
import com.omeralkan.applicationmicroservice.exception.BusinessException;
import com.omeralkan.applicationmicroservice.mapper.ApplicationMapper;
import com.omeralkan.applicationmicroservice.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private ParameterServiceClient parameterServiceClient;

    @InjectMocks
    private ApplicationServiceImpl applicationService;


    @Test
    void createApplication_ShouldCreateSuccessfully() {
        ApplicationRequestDto requestDto = new ApplicationRequestDto();
        requestDto.setCustomerId(1L);
        requestDto.setProductId(1L);
        requestDto.setDescription("Kasko başvurusu");
        requestDto.setPaymentTypeCode("T");
        requestDto.setInstallmentCount(6);

        CustomerResponseClientDto customer = new CustomerResponseClientDto();
        customer.setId(1L);
        customer.setAd("Ömer");
        customer.setSoyad("Alkan");

        ProductAmountResponseClientDto productAmount = new ProductAmountResponseClientDto();
        productAmount.setId(1L);
        productAmount.setProductId(1L);
        productAmount.setProductName("iPhone 15");
        productAmount.setAmount(new BigDecimal("50000"));

        PaymentTypeResponseClientDto paymentType = new PaymentTypeResponseClientDto();
        paymentType.setCode("T");
        paymentType.setName("Taksitli Ödeme");
        paymentType.setMinInstallment(2);
        paymentType.setMaxInstallment(12);

        ApplicationEntity entity = new ApplicationEntity();
        entity.setApplicationNumber("APP-2026-0001");
        entity.setStatus(ApplicationStatus.PENDING);

        ApplicationEntity savedEntity = new ApplicationEntity();
        savedEntity.setId(1L);
        savedEntity.setApplicationNumber("APP-2026-0001");
        savedEntity.setStatus(ApplicationStatus.PENDING);

        ApplicationResponseDto responseDto = new ApplicationResponseDto();
        responseDto.setId(1L);
        responseDto.setApplicationNumber("APP-2026-0001");
        responseDto.setCustomerName("Ömer Alkan");
        responseDto.setProductName("iPhone 15");
        responseDto.setAmount(new BigDecimal("50000"));
        responseDto.setStatus("PENDING");
        responseDto.setPaymentTypeCode("T");
        responseDto.setInstallmentCount(6);

        when(customerServiceClient.getCustomerById(1L)).thenReturn(customer);
        when(productServiceClient.getActiveAmountByProductId(1L)).thenReturn(productAmount);
        when(parameterServiceClient.getPaymentTypeByCode("T")).thenReturn(paymentType);
        when(applicationRepository.count()).thenReturn(0L);
        when(applicationMapper.toEntity(any(), any(), anyString())).thenReturn(entity);
        when(applicationRepository.save(any(ApplicationEntity.class))).thenReturn(savedEntity);
        when(applicationMapper.toResponse(any(), any(), any(), any())).thenReturn(responseDto);

        ApplicationResponseDto result = applicationService.createApplication(requestDto);

        assertNotNull(result);
        assertEquals("APP-2026-0001", result.getApplicationNumber());
        assertEquals("Ömer Alkan", result.getCustomerName());
        assertEquals("PENDING", result.getStatus());
        assertEquals("T", result.getPaymentTypeCode());
        assertEquals(6, result.getInstallmentCount());

        verify(customerServiceClient, times(1)).getCustomerById(1L);
        verify(productServiceClient, times(1)).getActiveAmountByProductId(1L);
        verify(parameterServiceClient, times(1)).getPaymentTypeByCode("T");
        verify(applicationRepository, times(1)).save(any(ApplicationEntity.class));
    }


    @Test
    void createApplication_WhenCustomerServiceFails_ShouldThrowBusinessException() {
        ApplicationRequestDto requestDto = new ApplicationRequestDto();
        requestDto.setCustomerId(999L);
        requestDto.setProductId(1L);
        requestDto.setPaymentTypeCode("T");
        requestDto.setInstallmentCount(6);

        when(customerServiceClient.getCustomerById(999L)).thenThrow(new RuntimeException("Connection refused"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            applicationService.createApplication(requestDto);
        });

        assertEquals("APP-CUST-ERR", exception.getMessage());
        verify(productServiceClient, never()).getActiveAmountByProductId(any());
        verify(parameterServiceClient, never()).getPaymentTypeByCode(any());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
    }


    @Test
    void createApplication_WhenInvalidInstallmentCount_ShouldThrowBusinessException() {
        ApplicationRequestDto requestDto = new ApplicationRequestDto();
        requestDto.setCustomerId(1L);
        requestDto.setProductId(1L);
        requestDto.setPaymentTypeCode("T");
        requestDto.setInstallmentCount(15); // Max 12, 15 geçersiz

        CustomerResponseClientDto customer = new CustomerResponseClientDto();
        customer.setId(1L);
        customer.setAd("Ömer");
        customer.setSoyad("Alkan");

        ProductAmountResponseClientDto productAmount = new ProductAmountResponseClientDto();
        productAmount.setId(1L);
        productAmount.setAmount(new BigDecimal("50000"));

        PaymentTypeResponseClientDto paymentType = new PaymentTypeResponseClientDto();
        paymentType.setCode("T");
        paymentType.setMinInstallment(2);
        paymentType.setMaxInstallment(12); // Max 12

        when(customerServiceClient.getCustomerById(1L)).thenReturn(customer);
        when(productServiceClient.getActiveAmountByProductId(1L)).thenReturn(productAmount);
        when(parameterServiceClient.getPaymentTypeByCode("T")).thenReturn(paymentType);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            applicationService.createApplication(requestDto);
        });

        assertEquals("APP-INST-400", exception.getMessage());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
    }


    @Test
    void getApplicationById_WhenNotFound_ShouldThrowBusinessException() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            applicationService.getApplicationById(999L);
        });

        assertEquals("APP-404", exception.getMessage());
    }


    @Test
    void deleteApplication_ShouldSetIsActiveFalse() {
        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(1L);
        entity.setApplicationNumber("APP-2026-0001");
        entity.setIsActive(true);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(applicationRepository.save(any(ApplicationEntity.class))).thenReturn(entity);

        applicationService.deleteApplication(1L);

        assertFalse(entity.getIsActive());
        verify(applicationRepository, times(1)).save(entity);
    }
}