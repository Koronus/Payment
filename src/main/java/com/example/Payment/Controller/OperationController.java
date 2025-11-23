package com.example.Payment.Controller;

import com.example.Payment.Dto.Mapping.OperationMapper;
import com.example.Payment.Dto.OperationResponseDTO;
import com.example.Payment.Service.OperationService;
import com.example.Payment.Tables.Operation;
import com.example.Payment.Validation.InputValid;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Controller
@RequestMapping("/payment-form")
public class OperationController {

    @Autowired
    private OperationMapper operationMapper;
    @Autowired
    private OperationService operationService;

    // Явный endpoint для демо версии
    @GetMapping("/demo")
    public String showDemoForm(Model model) {
        String paymentId = "demo-" + System.currentTimeMillis();

        model.addAttribute("paymentId", paymentId);


        return "payment-form";
    }



    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@Valid @ModelAttribute InputValid inputValidDTO,
                                            BindingResult bindingResult) {

        // Обрабатываем ошибки валидации
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });

            // Возвращаем ошибки клиенту
            return ResponseEntity.badRequest().body(errors);
        }

        // Если ошибок нет - обрабатываем платеж
        try {
            Operation operation = operationMapper.toEntity(inputValidDTO);
            Operation savedOperation = operationService.save(operation);

            boolean paymentSuccess = emulatePaymentProcessing();
            savedOperation.setStatus(paymentSuccess ? "SUCCESS" : "FAILED");
            operationService.save(savedOperation);

            OperationResponseDTO responseDTO = operationMapper.toResponseDTO(savedOperation);

            // Всегда возвращаем 200, но фронтенд проверяет статус
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка обработки платежа", "status", "ERROR"));
        }
    }

    private boolean emulatePaymentProcessing() {
        Random random = new Random();
        return random.nextInt(100) < 80;
    }
}

