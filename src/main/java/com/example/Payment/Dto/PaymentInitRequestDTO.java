package com.example.Payment.Dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentInitRequestDTO {
    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    @NotBlank(message = "Название товара/услуги обязательно")
    private String product;

    private String orderId;
    private String returnUrl;
    private String email;
    private String callbackUrl;
}
