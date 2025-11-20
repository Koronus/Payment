package com.example.Payment.Dto.Mapping;


import com.example.Payment.Dto.OperationCreateRequestDTO;
import com.example.Payment.Dto.OperationResponseDTO;
import com.example.Payment.Dto.OperationStatusDTO;
import com.example.Payment.Tables.Operation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OperationMapper {

    public Operation toEntity(OperationCreateRequestDTO dto) {
        Operation operation = new Operation();
        operation.setSurname(dto.getSurname());
        operation.setName_user(dto.getNameUser());
        operation.setPatronymic(dto.getPatronymic());
        operation.setAmount(dto.getAmount());
        operation.setPurpose(dto.getPurpose());
        operation.setCard_number(dto.getCardNumber());
        operation.setStatus("PENDING"); // Статус по умолчанию
        operation.setCreated_at(LocalDateTime.now());
        return operation;
    }

    public OperationResponseDTO toResponseDTO(Operation operation) {
        return new OperationResponseDTO(operation);
    }

    public OperationStatusDTO toStatusDTO(Operation operation) {
        return new OperationStatusDTO(operation);
    }
}
