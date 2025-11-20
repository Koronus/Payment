package com.example.Payment.Dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PaymentInitRequestDTO {

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    @NotBlank(message = "Описание платежа обязательно")
    @Size(max = 255)
    private String description;

    @NotBlank(message = "Order ID обязателен")
    private String orderId;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String customerEmail;

    // Геттеры и сеттеры
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
}
