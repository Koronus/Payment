package com.example.Payment.Service;


import com.example.Payment.Repository.OperationRepository;
import com.example.Payment.Tables.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationService {

    @Autowired
    private OperationRepository operationRepository;

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

}