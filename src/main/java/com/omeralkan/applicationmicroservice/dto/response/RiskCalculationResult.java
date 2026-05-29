package com.omeralkan.applicationmicroservice.dto.response;

import java.math.BigDecimal;

public record RiskCalculationResult(
        BigDecimal finalPremium,
        boolean isReportRequired
) {}