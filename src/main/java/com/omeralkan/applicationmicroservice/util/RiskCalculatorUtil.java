package com.omeralkan.applicationmicroservice.util;

import java.math.BigDecimal;

public final class RiskCalculatorUtil {

    private RiskCalculatorUtil() {
        throw new UnsupportedOperationException("Utility sınıfı nesneye dönüştürülemez!");
    }

    public static double calculateBmi(Integer height, BigDecimal weight) {
        if (height == null || weight == null || height <= 0) {
            return 0.0;
        }

        double heightInMeters = height / 100.0;
        return weight.doubleValue() / (heightInMeters * heightInMeters);
    }
}