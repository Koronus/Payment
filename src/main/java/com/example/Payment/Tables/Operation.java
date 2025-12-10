package com.example.Payment.Tables;

import com.example.Payment.Converter.SecureCardNumberConverter;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operations") // Сущность мапится на СУЩЕСТВУЮЩУЮ таблицу
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operations_id", insertable = false) // Маппинг на существующий столбец
    private Integer operations_Id;

    @Column(name = "surname") // Маппинг на существующий столбец
    private String surname;

    @Column(name = "name_user") // Маппинг на существующий столбец
    private String name_user;

    @Column(name = "patronymic") // Маппинг на существующий столбец
    private String patronymic;

    @Column(name = "amount", precision = 19, scale = 2) // Маппинг на существующий столбец
    private BigDecimal amount;

    @Column(name = "purpose") // Маппинг на существующий столбец
    private String purpose;

    @Column(name = "card_number") // Маппинг на существующий столбец
    @Convert(converter = SecureCardNumberConverter.class) // ← ЭТА АННОТАЦИЯ ОБЯЗАТЕЛЬНА!
    private String card_number;

    @Column(name = "status") // Маппинг на существующий столбец
    private String status;

    @Column(name = "created_at") // Маппинг на существующий столбец
    private LocalDateTime created_at;


    // Конструкторы
    public Operation() {
        // Пустой конструктор обязателен для JPA
    }


    public Operation(Integer Operations_ID, String surname, String name_user, String patronymic, BigDecimal amount, String purpose, String card_number, String status, LocalDateTime created_at) {
        this.operations_Id = operations_Id;
        this.surname = surname;
        this.name_user = name_user;
        this.patronymic = patronymic;
        this.amount = amount;
        this.purpose=purpose;
        this.card_number=card_number;
        this.status=status;
        this.created_at=created_at;
    }

    // Геттеры и сеттеры

    public Integer getOperations_Id() {
        return operations_Id;
    }

    public void setOperations_Id(Integer operations_Id) {
        this.operations_Id = operations_Id;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName_user() {
        return name_user;
    }

    public void setName_user(String name_user) {
        this.name_user = name_user;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }




}