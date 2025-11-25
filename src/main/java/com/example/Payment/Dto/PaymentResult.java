package com.example.Payment.Dto;



public class PaymentResult {
    private final boolean success;
    private final String message;
    private final String errorCode;

    private PaymentResult(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    // ✅ Статические методы-фабрики для удобства
    public static PaymentResult success(String message) {
        return new PaymentResult(true, message, null);
    }

    public static PaymentResult failed(String message) {
        return new PaymentResult(false, message, null);
    }

    public static PaymentResult failed(String message, String errorCode) {
        return new PaymentResult(false, message, errorCode);
    }

    // Геттеры
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}