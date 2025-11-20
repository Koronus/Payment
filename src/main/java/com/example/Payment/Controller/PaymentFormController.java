package com.example.Payment.Controller;


import com.example.Payment.Service.OperationService;
import com.example.Payment.Tables.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/payment-form")
public class PaymentFormController {

    @Autowired
    private OperationService operationService;

    // Основной endpoint для формы с paymentId
    @GetMapping
    public String showPaymentForm(@RequestParam(required = false) String paymentId, Model model) {
        if (paymentId == null || paymentId.isEmpty()) {
            paymentId = "demo-" + System.currentTimeMillis();
        }

        model.addAttribute("paymentId", paymentId);
        model.addAttribute("amount", "1500.00");
        model.addAttribute("purpose", "Демонстрационный платеж");

        return "payment-form";
    }

    // Явный endpoint для демо версии
    @GetMapping("/demo")
    public String showDemoForm(Model model) {
        String paymentId = "demo-" + System.currentTimeMillis();

        model.addAttribute("paymentId", paymentId);
        model.addAttribute("amount", "1500.00");
        model.addAttribute("purpose", "Демонстрационный платеж");

        return "payment-form";
    }

    // Обработка платежа
    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<Map<String, String>> processPayment(
            @RequestParam String surname,
            @RequestParam String nameUser,
            @RequestParam String patronymic,
            @RequestParam String amount,
            @RequestParam String purpose,
            @RequestParam String cardNumber,
            @RequestParam String email) {

        try {
            // Создаем операцию
            Operation operation = new Operation();
            operation.setSurname(surname);
            operation.setName_user(nameUser);
            operation.setPatronymic(patronymic);
            operation.setAmount(new BigDecimal(amount));
            operation.setPurpose(purpose);
            operation.setCard_number(cardNumber);
            operation.setStatus("PENDING");
            operation.setCreated_at(LocalDateTime.now());

            // Сохраняем в базу
            Operation savedOperation = operationService.save(operation);
            System.out.println("Создана операция с ID: " + savedOperation.getOperations_Id());

            // Эмуляция обработки платежа
            boolean paymentSuccess = emulatePaymentProcessing();

            if (paymentSuccess) {
                savedOperation.setStatus("SUCCESS");
                operationService.save(savedOperation);
                System.out.println("Статус обновлен на SUCCESS");

                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Платеж успешно обработан");
                response.put("operationId", savedOperation.getOperations_Id().toString());
                return ResponseEntity.ok(response);
            } else {
                savedOperation.setStatus("FAILED");
                operationService.save(savedOperation);
                System.out.println("Статус обновлен на FAILED");

                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Ошибка обработки платежа");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private boolean emulatePaymentProcessing() {
        Random random = new Random();
        return random.nextInt(100) < 80;
    }
}