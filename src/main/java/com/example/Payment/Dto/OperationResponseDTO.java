package com.example.Payment.Dto;

import com.example.Payment.Tables.Operation;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperationResponseDTO {
    private Integer operationsId;
    private String cardholderName;
    private BigDecimal amount;
    private String purpose;
    private String maskedCardNumber; // Маскированный номер карты для безопасности
    private String status;
    private String errorCode; // Код ошибки
    private String errorDetails; // Детали ошибки
    private String gatewayMessage;
    private LocalDateTime createdAt;

    // Конструктор из Entity
    public OperationResponseDTO(Operation operation) {
        this.operationsId = operation.getOperations_Id();
        this.cardholderName = operation.getCardholderName();
        this.amount = operation.getAmount();
        this.purpose = operation.getPurpose();
        this.maskedCardNumber = maskCardNumber(operation.getCard_number());
        this.status = operation.getStatus();
        this.createdAt = operation.getCreated_at();
        this.errorCode = operation.getErrorCode();
        this.errorDetails = operation.getErrorDetails();
        this.gatewayMessage = operation.getGatewayMessage();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return "****";
        return cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    // Геттеры.

    public String getCardholderName() {
        return cardholderName;
    }
    public Integer getOperationsId() { return operationsId; }
    public String getSurname() { return cardholderName; }
    public BigDecimal getAmount() { return amount; }
    public String getPurpose() { return purpose; }
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public String getGatewayMessage() {
        return gatewayMessage;
    }
}