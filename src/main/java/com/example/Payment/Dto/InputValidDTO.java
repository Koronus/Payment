package com.example.Payment.Dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class InputValidDTO {

    @NotBlank(message = "Фамилия обязательна")
    @Size(min = 2 , max = 50, message = "Фамилия не должна быть меньше 2 символов и превышать 50 символов")
    private String surname;

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 50, message = "Имя не должно  быть меньше 2 символов и превышать 50 символов")
    private String nameUser;

    @Size(min = 2, max = 50, message = "Отчество не должно  быть меньше 2 символов и превышать 50 символов")
    private String patronymic;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @Digits(integer = 10, fraction = 2, message = "Неверный формат суммы")
    private BigDecimal amount;

    @NotBlank(message = "Назначение платежа обязательно")
    @Size(max = 255, message = "Назначение платежа не должно превышать 255 символов")
    private String purpose;

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "^[0-9\\s]{16,19}$", message = "Неверный формат номера карты")
    private String cardNumber;

    @NotBlank(message = "Дата действия карты обязательна")
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/(2[4-9]|[3-9][0-9])$", message = "Неверный формат даты действия карты")
    private String dateOfAction;

    @NotBlank(message = "CVV код обязателен")
    @Pattern(regexp = "^[0-9]{3}$", message = "Неверный формат номера CVV")
    private String cvvCode;

    @Email
    private String email;


    // Геттеры и сеттеры
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getNameUser() { return nameUser; }
    public void setNameUser(String nameUser) { this.nameUser = nameUser; }

    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfAction() {
        return dateOfAction;
    }

    public void setDateOfAction(String dateOfAction) {
        this.dateOfAction = dateOfAction;
    }

    public String getCvvCode() {
        return cvvCode;
    }

    public void setCvvCode(String cvvCode) {
        this.cvvCode = cvvCode;
    }
}
