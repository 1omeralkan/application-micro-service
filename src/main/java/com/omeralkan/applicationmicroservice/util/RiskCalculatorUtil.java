package com.omeralkan.applicationmicroservice.util;

import java.math.BigDecimal;

public final class RiskCalculatorUtil {

    private RiskCalculatorUtil() {
        throw new UnsupportedOperationException("Utility sınıfı nesneye dönüştürülemez!");
    }

    public static double calculateBmi(BigDecimal height, BigDecimal weight) {
        if (height == null || weight == null || height.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }

        double heightInMeters = height.doubleValue() / 100.0;
        return weight.doubleValue() / (heightInMeters * heightInMeters);
    }
}