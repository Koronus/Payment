package com.example.Payment.Tables;

import jakarta.persistence.*;

@Entity
@Table(name = "Users") // Указываем имя таблицы в БД
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_id_seq", allocationSize = 1)
    @Column(name = "Operations_ID")
    private Long Operations_ID;

    @Column(name = "amount", length = 255)
    private String amount;

    @Column(name = "purpose", length = 255)
    private String purpose;

    @Column(name = "created_at", length = 255)
    private String created_at;



    @Column(name = "User_ID", length = 255)
    private Long User_ID;

    // Конструкторы
    public Operation() {
        // Пустой конструктор обязателен для JPA
    }

    public Operation(Long Operations_ID, String amount, String purpose, String created_at, Long User_ID) {
        this.Operations_ID = Operations_ID;
        this.amount = amount;
        this.purpose = purpose;
        this.created_at = created_at;
        this.User_ID = User_ID;
    }

    // Геттеры и сеттеры

    public Long getOperations_ID() {
        return Operations_ID;
    }

    public void setOperations_ID(Long operations_ID) {
        Operations_ID = operations_ID;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
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
        return User_ID;
    }

    public void setUser_ID(Long user_ID) {
        User_ID = user_ID;
    }

    // toString метод для удобства отладки
    @Override
    public String toString() {
        return "Operation{" +
                "Operations_ID=" + Operations_ID +
                ", amount='" + amount + '\'' +
                ", purpose='" + purpose + '\'' +
                ", created_at='" + created_at + '\'' +
                ", User_ID='" + User_ID + '\'' +
                '}';
    }
}