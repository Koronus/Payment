package com.example.Payment.Tables;

import jakarta.persistence.*;

@Entity
@Table(name = "status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_id_seq", allocationSize = 1)
    @Column(name = "StatusID")
    private Long Status_ID;

    @Column(name = "status_name", length = 50)
    private String status_name;

    @Column(name = "status_date", length = 255)
    private String status_date;

    @Column(name = "comment_operation", length = 255)
    private String comment_operation;



    @Column(name = "Operations_ID", length = 255)
    private Long operations_ID;

    // Конструкторы
    public Status() {
        // Пустой конструктор обязателен для JPA
    }

    public Status(Long Status_ID, String status_name, String status_date, String comment_operation,Long operations_ID) {
        this.Status_ID = Status_ID;
        this.status_name = status_name;
        this.status_date = status_date;
        this.comment_operation = comment_operation;
        this.operations_ID = operations_ID;
    }

    // Геттеры и сеттеры


    public Long getStatus_ID() {
        return Status_ID;
    }

    public void setStatus_ID(Long status_ID) {
        Status_ID = status_ID;
    }

    public String getStatus_date() {
        return status_date;
    }

    public void setStatus_date(String status_date) {
        this.status_date = status_date;
    }

    public String getStatus_name() {
        return status_name;
    }

    public void setStatus_name(String status_name) {
        this.status_name = status_name;
    }

    public String getComment_operation() {
        return comment_operation;
    }

    public void setComment_operation(String comment_operation) {
        this.comment_operation = comment_operation;
    }

    public Long getOperations_ID() {
        return operations_ID;
    }

    public void setOperations_ID(Long operations_ID) {
        this.operations_ID = operations_ID;
    }

    // toString метод для удобства отладки
    @Override
    public String toString() {
        return "Status{" +
                "Status_ID=" + Status_ID +
                ", status_name='" + status_name + '\'' +
                ", status_date='" + status_date + '\'' +
                ", comment_operation='" + comment_operation + '\'' +
                ", Operations_ID='" + operations_ID + '\'' +
                '}';
    }
}
