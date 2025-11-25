package com.example.Payment.Controller;

import com.example.Payment.Dto.Mapping.OperationMapper;
import com.example.Payment.Dto.OperationResponseDTO;
import com.example.Payment.Dto.PaymentResult;
import com.example.Payment.Service.OperationService;
import com.example.Payment.Tables.Operation;
import com.example.Payment.Dto.InputValidDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

        // Обрабатываем ошибки валидации
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors);
        }

        // Если ошибок нет - обрабатываем платеж
        try {
            Operation operation = operationMapper.toEntity(inputValidDTO);
            Operation savedOperation = operationService.save(operation);

            //  ИЗМЕНЕНИЕ: Используем улучшенную эмуляцию
            PaymentResult paymentResult = emulatePaymentProcessing();

            savedOperation.setStatus(paymentResult.isSuccess() ? "SUCCESS" : "FAILED");
            //  Сохраняем причину ошибки если есть
            if (!paymentResult.isSuccess()) {
                savedOperation.setErrorReason(paymentResult.getMessage());
            }

            operationService.save(savedOperation);

            OperationResponseDTO responseDTO = operationMapper.toResponseDTO(savedOperation);

            //  ИЗМЕНЕНИЕ: Возвращаем соответствующий статус HTTP
            if (paymentResult.isSuccess()) {
                return ResponseEntity.ok(responseDTO);
            } else {
                // Для ошибок платежа возвращаем 200, но с status: "error"
                // Или можно использовать 402 Payment Required, 400 Bad Request и т.д.
                return ResponseEntity.ok(responseDTO); // Фронтенд сам проверит status
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка обработки платежа", "status", "ERROR"));
        }
    }

    // ✅ СТАРАЯ ВЕРСИЯ (удалить или оставить как запасной вариант)
    // private boolean emulatePaymentProcessing() {
    //     Random random = new Random();
    //     return random.nextInt(100) < 80;
    // }

    // ✅ НОВАЯ ВЕРСИЯ: Улучшенная эмуляция с разными сценариями
    private PaymentResult emulatePaymentProcessing() {
        Random random = new Random();
        int scenario = random.nextInt(100);

        if (scenario < 50) {
            return PaymentResult.success("Платеж успешно обработан");
        } else if (scenario < 55) {
            return PaymentResult.failed("Недостаточно средств на карте");
        } else if (scenario < 60) {
            return PaymentResult.failed("Карта заблокирована");
        } else if (scenario < 65) {
            return PaymentResult.failed("Превышен лимит по карте");
        } else if (scenario < 70) {
            return PaymentResult.failed("Ошибка связи с банком");
        } else {
            return PaymentResult.failed("Операция отклонена банком");
        }
    }
}