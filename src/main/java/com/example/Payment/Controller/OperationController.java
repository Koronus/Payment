package com.example.Payment.Controller;

import com.example.Payment.Dto.InputValidDTO;
import com.example.Payment.Dto.Mapping.OperationMapper;
import com.example.Payment.Dto.OperationResponseDTO;
import com.example.Payment.Service.OperationService;
import com.example.Payment.Entity.Operation;
//import com.example.Payment.Validation.InputValid;
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
    public ResponseEntity<?> processPayment(@Valid @ModelAttribute InputValidDTO inputValidDTO,
                                            BindingResult bindingResult) {

        try {
            System.out.println("🟢 Начало обработки платежа: " + inputValidDTO);

            // 1. Маппинг DTO -> Entity
            Operation operation = operationMapper.toEntity(inputValidDTO);
            System.out.println("📝 Создана операция: " + operation);

            // 2. Сохраняем PENDING статус
            System.out.println("💾 Первое сохранение (PENDING)...");
            Operation savedOperation = operationService.save(operation);
            System.out.println("✅ Сохранено с ID: " + savedOperation.getOperations_Id());

            // 3. Эмулируем платеж
            boolean paymentSuccess = emulatePaymentProcessing();
            String status = paymentSuccess ? "SUCCESS" : "FAILED";
            savedOperation.setStatus(status);

            // 4. Обновляем статус
            System.out.println("💾 Второе сохранение (" + status + ")...");
            Operation updatedOperation = operationService.save(savedOperation);
            System.out.println("✅ Обновлено, ID: " + updatedOperation.getOperations_Id());

            // 5. Возвращаем ответ
            OperationResponseDTO responseDTO = operationMapper.toResponseDTO(updatedOperation);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка обработки платежа"));
        }
    }

    private boolean emulatePaymentProcessing() {
        Random random = new Random();
        return random.nextInt(100) < 80;
    }
}