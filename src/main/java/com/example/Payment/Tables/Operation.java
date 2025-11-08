package com.example.Payment.Tables;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Operations") // Указываем имя таблицы в БД
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "operation_seq")
    @SequenceGenerator(name = "operation_seq", sequenceName = "operation_id_seq", allocationSize = 1)
    @Column(name = "Operations_ID")
    private Long Operations_ID;

    @Column(name = "amount",  precision = 10, scale = 2)
    private BigDecimal amount;              //BigDecimal для денежных сумм

    @Column(name = "purpose", length = 255)
    private String purpose;

    @Column(name = "created_at", length = 255)
    private String created_at;



    @Column(name = "User_ID", length = 255)
    private Long userID;

    // Конструкторы
    public Operation() {
        // Пустой конструктор обязателен для JPA
    }

    public Operation(Long Operations_ID, BigDecimal amount, String purpose, String created_at, Long userID) {
        this.Operations_ID = Operations_ID;
        this.amount = amount;
        this.purpose = purpose;
        this.created_at = created_at;
        this.userID = userID;
    }

    // Геттеры и сеттеры

    public Long getOperations_ID() {
        return Operations_ID;
    }

    public void setOperations_ID(Long operations_ID) {
        Operations_ID = operations_ID;
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

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Long getUser_ID() {
        return userID;
    }

    public void setUser_ID(Long user_ID) {
        userID = user_ID;
    }

    // toString метод для удобства отладки
    @Override
    public String toString() {
        return "Operation{" +
                "Operations_ID=" + Operations_ID +
                ", amount='" + amount + '\'' +
                ", purpose='" + purpose + '\'' +
                ", created_at='" + created_at + '\'' +
                ", User_ID='" + userID+ '\'' +
                '}';
    }
}