package com.example.Payment.Service;

import com.example.Payment.Dto.*;
import com.example.Payment.Dto.Mapping.OperationMapper;
import com.example.Payment.Repository.OperationRepository;
import com.example.Payment.Tables.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationService {

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private OperationMapper operationMapper;


    public Operation save(Operation operation) {
        return operationRepository.save(operation);
    }

    // Возвращаем DTO вместо Entity
    public List<OperationResponseDTO> getAllOperations() {
        return operationRepository.findAll().stream()
                .map(operationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Возвращаем DTO вместо Entity
    public OperationResponseDTO findById(Integer id) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operation not found"));
        return operationMapper.toResponseDTO(operation);
    }

    // Принимаем DTO, возвращаем DTO
    public OperationResponseDTO createOperation(OperationCreateRequestDTO requestDTO) {
        Operation operation = operationMapper.toEntity(requestDTO);
        Operation savedOperation = operationRepository.save(operation);
        return operationMapper.toResponseDTO(savedOperation);
    }

    // Для внутреннего использования (если нужно)
    protected Operation findEntityById(Integer id) {
        return operationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operation not found"));
    }
}