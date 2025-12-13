package com.example.Payment.Dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperationCreateRequestDTO {

    @NotBlank(message = "Фимилия и имя владельца обязательно")
    @Pattern(regexp = "^[A-Za-z\\s\\.]+$",
            message = "Только латинские буквы, как на карте")
    private String cardholderName;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @Digits(integer = 10, fraction = 2, message = "Неверный формат суммы")
    private BigDecimal amount;

    @NotBlank(message = "Назначение платежа обязательно")
    @Size(max = 255, message = "Назначение платежа не должно превышать 255 символов")
    private String purpose;

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "^(\\d[ ]*){16,19}$", message = "Неверный формат номера карты")
    private String cardNumber;

    private String status;

    public LocalDateTime createdAt;

    // Геттеры и сеттеры
    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }



    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}