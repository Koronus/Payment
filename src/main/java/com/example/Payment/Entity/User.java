package com.example.Payment.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(name = "Users") // Указываем имя таблицы в БД
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "surname", length = 255)
    private String surname;

    @Column(name = "name_user", length = 255)
    private String nameUser;

    @Column(name = "patronymic", length = 255)
    private String patronymic;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "Created_At", length = 255)
    private LocalDateTime Created_At;

    @Column(name = "Updated_At", length = 255)
    private LocalDateTime Updated_At;

    // Автоматическое установление случайного id и текущего времени создания
    @PrePersist
    protected void onCreate() {
        if (userId == null) {
            userId = UUID.randomUUID();
        }
        Created_At = LocalDateTime.now();
        Updated_At = LocalDateTime.now();
    }

    // Автоматическое обновление времени при изменении
    @PreUpdate
    protected void onUpdate() {
        Updated_At = LocalDateTime.now();
    }

    private boolean completed;

    // Конструкторы
    public User() {
        // Пустой конструктор обязателен для JPA
    }

    public User(UUID userId, String surname, String nameUser, String patronymic, String email,LocalDateTime Created_At, LocalDateTime Updated_At,boolean completed) {
        this.userId = userId;
        this.surname = surname;
        this.nameUser = nameUser;
        this.patronymic = patronymic;
        this.email = email;
        this.Created_At = Created_At;
        this.Updated_At = Updated_At;
        this.completed = completed;
    }




    // Геттеры и сеттеры
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreated_At() {
        if (Created_At == null) return null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return Created_At.format(formatter);
    }

    protected void setCreated_At(LocalDateTime Created_At) {this.Created_At = Created_At;}

    public String getUpdated_At() {
        if (Updated_At == null) return null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return Updated_At.format(formatter);
    }

    public void setUpdated_At(LocalDateTime Updated_At) {this.Updated_At = Updated_At;}

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // toString метод для удобства отладки
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", surname='" + surname + '\'' +
                ", nameUser='" + nameUser + '\'' +
                ", patronymic='" + patronymic + '\'' +
                ", email='" + email + '\'' +
                ", Created_At='" + Created_At + '\'' +
                ", Updated_At='" + Updated_At + '\'' +
                '}';
    }
}