package com.example.Payment.Dto.Mapping;


//import com.example.Payment.Dto.OperationCreateRequestDTO;
//import com.example.Payment.Dto.OperationResponseDTO;
//import com.example.Payment.Dto.OperationStatusDTO;
import com.example.Payment.Dto.OperationResponseDTO;
import com.example.Payment.Entity.Operation;
import com.example.Payment.Dto.InputValidDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OperationMapper {

   public Operation toEntity(InputValidDTO dto) {
        System.out.println("🔧 OperationMapper: маппинг DTO в Entity");
        Operation operation = new Operation();
        operation.setSurname(dto.getSurname());
        operation.setNameUser(dto.getNameUser());
        operation.setPatronymic(dto.getPatronymic());
        operation.setAmount(dto.getAmount());
        operation.setPurpose(dto.getPurpose());
        operation.setCard_number(dto.getCardNumber());
        operation.setStatus("PENDING"); // Статус по умолчанию
        operation.setCreated_at(LocalDateTime.now());
        operation.setErrorReason(dto.getErrorReason());
        operation.setErrorCode(dto.getErrorCode());
        return operation;
    }

    public OperationResponseDTO toResponseDTO(Operation operation) {
        return new OperationResponseDTO(operation);
    }
//
//    public OperationStatusDTO toStatusDTO(Operation operation) {
//        return new OperationStatusDTO(operation);
//    }
}