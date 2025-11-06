package com.example.Payment.Service;

import com.example.Payment.Repository.BankingRepository;

import com.example.Payment.Tables.Banking_Details;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankingService {
    @Autowired
    private BankingRepository bankingRepository;

    public List<Banking_Details> getAllBanking_Details() {
        return bankingRepository.findAll();
    }
}
