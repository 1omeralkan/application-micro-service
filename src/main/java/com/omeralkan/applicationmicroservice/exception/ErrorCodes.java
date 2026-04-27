package com.omeralkan.applicationmicroservice.exception;

public final class ErrorCodes {

    private ErrorCodes() {}

    public static final String APPLICATION_NOT_FOUND = "APP-404";
    public static final String CUSTOMER_NOT_FOUND = "APP-CUST-404";
    public static final String PRODUCT_AMOUNT_NOT_FOUND = "APP-AMT-404";
    public static final String CUSTOMER_SERVICE_ERROR = "APP-CUST-ERR";
    public static final String PRODUCT_SERVICE_ERROR = "APP-PROD-ERR";
}