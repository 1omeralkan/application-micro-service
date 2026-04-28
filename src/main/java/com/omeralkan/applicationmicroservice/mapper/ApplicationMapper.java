package com.omeralkan.applicationmicroservice.mapper;

import com.omeralkan.applicationmicroservice.client.CustomerResponseClientDto;
import com.omeralkan.applicationmicroservice.client.PaymentTypeResponseClientDto;
import com.omeralkan.applicationmicroservice.client.ProductAmountResponseClientDto;
import com.omeralkan.applicationmicroservice.dto.request.ApplicationRequestDto;
import com.omeralkan.applicationmicroservice.dto.response.ApplicationResponseDto;
import com.omeralkan.applicationmicroservice.entity.ApplicationEntity;
import com.omeralkan.applicationmicroservice.entity.ApplicationStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class ApplicationMapper {

    public ApplicationResponseDto toResponse(ApplicationEntity entity,
                                             CustomerResponseClientDto customer,
                                             ProductAmountResponseClientDto productAmount,
                                             PaymentTypeResponseClientDto paymentType) {
        ApplicationResponseDto dto = new ApplicationResponseDto();
        dto.setId(entity.getId());
        dto.setApplicationNumber(entity.getApplicationNumber());
        dto.setCustomerId(entity.getCustomerId());
        dto.setCustomerName(customer.getAd() + " " + customer.getSoyad());
        dto.setProductId(entity.getProductId());
        dto.setProductName(productAmount.getProductName());
        dto.setProductAmountId(entity.getProductAmountId());
        dto.setAmount(productAmount.getAmount());
        dto.setApplicationDate(entity.getApplicationDate());
        dto.setStatus(entity.getStatus().name());
        dto.setDescription(entity.getDescription());
        dto.setPaymentTypeCode(entity.getPaymentTypeCode());
        dto.setPaymentTypeName(paymentType.getName());
        dto.setInstallmentCount(entity.getInstallmentCount());
        dto.setInstallmentAmount(
                productAmount.getAmount().divide(
                        BigDecimal.valueOf(entity.getInstallmentCount()), 2, RoundingMode.HALF_UP));
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    public ApplicationEntity toEntity(ApplicationRequestDto requestDto,
                                      Long productAmountId,
                                      String applicationNumber) {
        ApplicationEntity entity = new ApplicationEntity();
        entity.setApplicationNumber(applicationNumber);
        entity.setCustomerId(requestDto.getCustomerId());
        entity.setProductId(requestDto.getProductId());
        entity.setProductAmountId(productAmountId);
        entity.setApplicationDate(LocalDate.now());
        entity.setStatus(ApplicationStatus.PENDING);
        entity.setDescription(requestDto.getDescription());
        entity.setPaymentTypeCode(requestDto.getPaymentTypeCode().toUpperCase());
        entity.setInstallmentCount(requestDto.getInstallmentCount());
        return entity;
    }
}