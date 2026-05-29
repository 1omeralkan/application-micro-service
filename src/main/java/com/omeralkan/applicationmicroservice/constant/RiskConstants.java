package com.omeralkan.applicationmicroservice.constant;

public final class RiskConstants {

    private RiskConstants() {
        throw new UnsupportedOperationException("Constant sınıfı new'lenemez!");
    }
    public static final String GENDER_MALE_CODE = "E";

    public static final String KEY_HEALTH_REPORT_THRESHOLD = "HEALTH_REPORT_THRESHOLD";
    public static final String KEY_BMI_MIN_LIMIT = "BMI_MIN_LIMIT";
    public static final String KEY_BMI_OBESE_LIMIT = "BMI_OBESE_LIMIT";
    public static final String KEY_BMI_OVERWEIGHT_LIMIT = "BMI_OVERWEIGHT_LIMIT";
    public static final String KEY_BMI_HIGH_RISK_PENALTY = "BMI_HIGH_RISK_PENALTY";
    public static final String KEY_BMI_MEDIUM_RISK_PENALTY = "BMI_MEDIUM_RISK_PENALTY";
    public static final String KEY_MAX_AGE_LIMIT = "MAX_AGE_LIMIT";
    public static final String KEY_AGE_PENALTY = "AGE_PENALTY";
    public static final String KEY_MALE_GENDER_PENALTY = "MALE_GENDER_PENALTY";
}