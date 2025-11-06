package com.example.Payment.Tables;

import jakarta.persistence.*;

@Entity
@Table(name = "Banking_Details") // Указываем имя таблицы в БД
public class Banking_Details {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_id_seq", allocationSize = 1)
    @Column(name = "Detalis_ID")
    private Long Details_ID;

    @Column(name = "Operations_ID", length = 255)
    private Integer Operations_ID;

    @Column(name = "Account_number", length = 20)
    private String Account_number;

    @Column(name = "Bank_bik", length = 9)
    private String Bank_bik;

    @Column(name = "recipient_phone", length = 15)
    private String recipient_phone;


    // Конструкторы
    public Banking_Details() {
        // Пустой конструктор обязателен для JPA
    }

    public Banking_Details(Long Detalis_ID, Integer Operations_ID, String Account_number, String Bank_bik, String recipient_phone) {
        this.Details_ID = Detalis_ID;
        this.Operations_ID = Operations_ID;
        this.Account_number = Account_number;
        this.Bank_bik = Bank_bik;
        this.recipient_phone = recipient_phone;
    }

    // Геттеры и сеттеры


    public Long getDetails_ID() {
        return Details_ID;
    }

    public void setDetails_ID(Long details_ID) {
        Details_ID = details_ID;
    }

    public Integer getOperations_ID() {
        return Operations_ID;
    }

    public void setOperations_ID(Integer operations_ID) {
        Operations_ID = operations_ID;
    }

    public String getAccount_number() {
        return Account_number;
    }

    public void setAccount_number(String account_number) {
        Account_number = account_number;
    }

    public String getBank_bik() {
        return Bank_bik;
    }

    public void setBank_bik(String bank_bik) {
        Bank_bik = bank_bik;
    }

    public String getRecipient_phone() {
        return recipient_phone;
    }

    public void setRecipient_phone(String recipient_phone) {
        this.recipient_phone = recipient_phone;
    }

    // toString метод для удобства отладки
    @Override
    public String toString() {
        return "Banking_Details{" +
                "Details_ID=" + Details_ID +
                ", Operations_ID='" + Operations_ID + '\'' +
                ", Bank_bik='" + Account_number + '\'' +
                ", created_at='" + Bank_bik + '\'' +
                ", recipient_phone='" + recipient_phone + '\'' +
                '}';
    }
}