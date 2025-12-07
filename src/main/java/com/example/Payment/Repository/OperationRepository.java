package com.example.Payment.Repository;

import com.example.Payment.Entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Integer> {

//    @Query("SELECT COUNT(o) FROM Operation o")
//    long count();



}
