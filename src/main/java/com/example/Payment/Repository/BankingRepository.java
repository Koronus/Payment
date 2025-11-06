package com.example.Payment.Repository;


import com.example.Payment.Tables.Banking_Details;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankingRepository extends JpaRepository<Banking_Details, Long> {


}
