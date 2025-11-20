package com.example.Payment.Dto;

import com.example.Payment.Tables.Operation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperationResponseDTO {
    private Integer operationsId;
    private String surname;
    private String nameUser;
    private String patronymic;
    private BigDecimal amount;
    private String purpose;
    private String maskedCardNumber; // Маскированный номер карты для безопасности
    private String status;
    private LocalDateTime createdAt;

    // Конструктор из Entity
    public OperationResponseDTO(Operation operation) {
        this.operationsId = operation.getOperations_Id();
        this.surname = operation.getSurname();
        this.nameUser = operation.getName_user();
        this.patronymic = operation.getPatronymic();
        this.amount = operation.getAmount();
        this.purpose = operation.getPurpose();
        this.maskedCardNumber = maskCardNumber(operation.getCard_number());
        this.status = operation.getStatus();
        this.createdAt = operation.getCreated_at();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return "****";
        return cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    // Геттеры
    public Integer getOperationsId() { return operationsId; }
    public String getSurname() { return surname; }
    public String getNameUser() { return nameUser; }
    public String getPatronymic() { return patronymic; }
    public BigDecimal getAmount() { return amount; }
    public String getPurpose() { return purpose; }
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}