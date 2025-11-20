package com.example.Payment.Dto;

import com.example.Payment.Tables.Operation;

import java.time.LocalDateTime;

public class OperationStatusDTO {
    private Integer operationsId;
    private String status;
    private String amount;
    private String purpose;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Конструктор из Entity
    public OperationStatusDTO(Operation operation) {
        this.operationsId = operation.getOperations_Id();
        this.status = operation.getStatus();
        this.amount = operation.getAmount().toString();
        this.purpose = operation.getPurpose();
        this.createdAt = operation.getCreated_at();
        // Если есть поле updatedAt в entity, иначе используем createdAt
        this.updatedAt = operation.getCreated_at();
    }

    // Геттеры
    public Integer getOperationsId() { return operationsId; }
    public String getStatus() { return status; }
    public String getAmount() { return amount; }
    public String getPurpose() { return purpose; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
