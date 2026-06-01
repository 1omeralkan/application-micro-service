package com.omeralkan.applicationmicroservice.service;

import com.omeralkan.applicationmicroservice.client.ProductAmountResponseClientDto;
import com.omeralkan.applicationmicroservice.dto.response.RiskCalculationResult;
import java.math.BigDecimal;

public interface RiskEngineService {
    RiskCalculationResult calculateRisk(ProductAmountResponseClientDto productAmount, Integer age, BigDecimal height, BigDecimal weight, String gender);
}