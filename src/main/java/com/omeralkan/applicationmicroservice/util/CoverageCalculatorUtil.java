package com.omeralkan.applicationmicroservice.util;

import com.omeralkan.applicationmicroservice.client.ProductCoverageResponseClientDto;
import com.omeralkan.applicationmicroservice.entity.ApplicationCoverageEntity;
import com.omeralkan.applicationmicroservice.entity.ApplicationEntity;

import java.math.BigDecimal;

public final class CoverageCalculatorUtil {

    private CoverageCalculatorUtil() {
        throw new UnsupportedOperationException("Utility sınıfı nesneye dönüştürülemez!");
    }

    public static ApplicationCoverageEntity createCoverageEntity(
            ProductCoverageResponseClientDto catalogCoverage,
            BigDecimal premiumAmount,
            ApplicationEntity application,
            BigDecimal multiplier) {

        BigDecimal calculatedAmount = premiumAmount.multiply(multiplier);

        ApplicationCoverageEntity coverageEntity = new ApplicationCoverageEntity();
        coverageEntity.setApplication(application);
        coverageEntity.setCoverageCode(catalogCoverage.getCoverageCode());
        coverageEntity.setName(catalogCoverage.getName());
        coverageEntity.setCalculatedAmount(calculatedAmount);

        return coverageEntity;
    }
}