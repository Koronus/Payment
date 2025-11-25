package com.example.Payment.Tables;

import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class Cards {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "cardID", insertable = false)
    private Long cardID;

    @Column(name = "cardNumber", length = 19)
    private String cardNumber;

    @Column(name = "cvvCode", length = 3)
    private String cvvCode;


}
