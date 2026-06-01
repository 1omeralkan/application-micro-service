package com.omeralkan.applicationmicroservice.service.impl;

import com.omeralkan.applicationmicroservice.client.ParameterServiceClient;
import com.omeralkan.applicationmicroservice.client.ProductAmountResponseClientDto;
import com.omeralkan.applicationmicroservice.constant.RiskConstants;
import com.omeralkan.applicationmicroservice.dto.response.RiskCalculationResult;
import com.omeralkan.applicationmicroservice.service.RiskEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import static com.omeralkan.applicationmicroservice.util.RiskCalculatorUtil.calculateBmi;

@Service
@RequiredArgsConstructor
public class RiskEngineServiceImpl implements RiskEngineService {

    private final ParameterServiceClient parameterServiceClient;

    @Override
    public RiskCalculationResult calculateRisk(ProductAmountResponseClientDto productAmount, Integer age, BigDecimal height, BigDecimal weight, String gender) {
        BigDecimal multiplier = BigDecimal.ONE;

        Map<String, BigDecimal> riskParams = parameterServiceClient.getAllRiskParameters();

        BigDecimal riskScoreThreshold = getRequiredParam(riskParams, RiskConstants.KEY_HEALTH_REPORT_THRESHOLD);
        BigDecimal bmiMinLimit = getRequiredParam(riskParams, RiskConstants.KEY_BMI_MIN_LIMIT);
        BigDecimal bmiObeseLimit = getRequiredParam(riskParams, RiskConstants.KEY_BMI_OBESE_LIMIT);
        BigDecimal bmiOverweightLimit = getRequiredParam(riskParams, RiskConstants.KEY_BMI_OVERWEIGHT_LIMIT);
        BigDecimal bmiHighRiskPenalty = getRequiredParam(riskParams, RiskConstants.KEY_BMI_HIGH_RISK_PENALTY);
        BigDecimal bmiMediumRiskPenalty = getRequiredParam(riskParams, RiskConstants.KEY_BMI_MEDIUM_RISK_PENALTY);
        BigDecimal maxAgeLimit = getRequiredParam(riskParams, RiskConstants.KEY_MAX_AGE_LIMIT);
        BigDecimal agePenalty = getRequiredParam(riskParams, RiskConstants.KEY_AGE_PENALTY);
        BigDecimal maleGenderPenalty = getRequiredParam(riskParams, RiskConstants.KEY_MALE_GENDER_PENALTY);

        double vki = calculateBmi(height, weight);

        if (vki < bmiMinLimit.doubleValue() || vki >= bmiObeseLimit.doubleValue()) {
            multiplier = multiplier.add(bmiHighRiskPenalty);
        } else if (vki >= bmiOverweightLimit.doubleValue() && vki < bmiObeseLimit.doubleValue()) {
            multiplier = multiplier.add(bmiMediumRiskPenalty);
        }

        if (age > maxAgeLimit.intValue()) {
            multiplier = multiplier.add(agePenalty);
        }

        if (RiskConstants.GENDER_MALE_CODE.equalsIgnoreCase(gender)) {
            multiplier = multiplier.add(maleGenderPenalty);
        }

        BigDecimal finalPremium = productAmount.getAmount().multiply(multiplier);
        boolean isReportRequired = multiplier.compareTo(riskScoreThreshold) > 0;

        return new RiskCalculationResult(finalPremium, isReportRequired);
    }

    private BigDecimal getRequiredParam(Map<String, BigDecimal> params, String key) {
        BigDecimal value = params.get(key);
        if (value == null) {
            throw new RuntimeException("Kritik risk parametresi bulunamadı! Lütfen Parameter Service'i kontrol edin. Eksik Key: " + key);
        }
        return value;
    }
}