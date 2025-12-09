package com.example.Payment.Controller;

import com.example.Payment.Service.OperationService;
import com.example.Payment.Tables.Operation;
import jakarta.servlet.http.HttpSession;
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
import java.util.concurrent.ThreadLocalRandom;

@Controller
@RequestMapping("/payment-form")
public class PaymentFormController {

    @Autowired
    private OperationService operationService;

    // ====== 1. Показ формы оплаты (GET /payment-form) ======
    @GetMapping
    public String showPaymentForm(@RequestParam(required = false) String paymentId,
                                  Model model) {

        if (paymentId == null || paymentId.isEmpty()) {
            paymentId = "demo-" + System.currentTimeMillis();
        }
        model.addAttribute("paymentId", paymentId);

        // Просто дефолтные значения для демо
        model.addAttribute("amount", "1500.00");
        model.addAttribute("purpose", "Демонстрационный платеж");

        // Никаких данных из сессии сюда не подставляем
        return "payment-form";
    }

    // ====== 2. Демо-страница (GET /payment-form/demo) ======
    @GetMapping("/demo")
    public String showDemoForm() {
        String paymentId = "demo-" + System.currentTimeMillis();
        // просто редиректим на основной GET с готовым paymentId
        return "redirect:/payment-form?paymentId=" + paymentId;
    }

    // ====== 3. Шаг 1: форма → генерация OTP (POST /payment-form/otp) ======
    @PostMapping("/otp")
    public String handlePaymentFormAndShowOtp(
            @RequestParam String surname,
            @RequestParam String nameUser,
            @RequestParam String patronymic,
            @RequestParam String amount,
            @RequestParam String purpose,
            @RequestParam String cardNumber,
            @RequestParam String email,
            HttpSession session,
            Model model) {

        // сохраняем все данные формы в сессию
        session.setAttribute("surname", surname);
        session.setAttribute("nameUser", nameUser);
        session.setAttribute("patronymic", patronymic);
        session.setAttribute("amount", amount);
        session.setAttribute("purpose", purpose);
        session.setAttribute("cardNumber", cardNumber);
        session.setAttribute("email", email);

        // генерируем 4-значный код
        int code = ThreadLocalRandom.current().nextInt(1000, 10000);
        session.setAttribute("otp", code);
        System.out.println("SMS code: " + code);

        // показываем страницу ввода кода
        return "SMSVerification"; // шаблон SMSVerification.html
    }

    // ====== 4. Шаг 2: проверка OTP (POST /payment-form/verify-otp) ======

    @PostMapping("/verify-otp")
    @ResponseBody
    public Map<String, Object> verifyOtp(@RequestBody Map<String, String> body,
                                         HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 1. Проверка кода
        Object otpFromSession = session.getAttribute("otp");
        String userOtp = body.get("otp");

        if (otpFromSession == null || userOtp == null) {
            response.put("otpValid", false);
            response.put("message", "Код не найден или истёк. Повторите оплату.");
            return response;
        }

        String expectedOtp = String.valueOf(otpFromSession);
        if (!expectedOtp.equals(userOtp)) {
            response.put("otpValid", false);
            response.put("message", "Неверный код");
            return response;
        }

        // Код корректный
        response.put("otpValid", true);

        try {
            // 2. Достаём данные формы из сессии
            String surname    = (String) session.getAttribute("surname");
            String nameUser   = (String) session.getAttribute("nameUser");
            String patronymic = (String) session.getAttribute("patronymic");
            String amountStr  = (String) session.getAttribute("amount");
            String purpose    = (String) session.getAttribute("purpose");
            String cardNumber = (String) session.getAttribute("cardNumber");
            String email      = (String) session.getAttribute("email");

            Operation operation = new Operation();
            operation.setSurname(surname);
            operation.setName_user(nameUser);
            operation.setPatronymic(patronymic);
            operation.setAmount(new BigDecimal(amountStr));
            operation.setPurpose(purpose);
            operation.setCard_number(cardNumber);
            operation.setStatus("PENDING");
            operation.setCreated_at(LocalDateTime.now());

            Operation savedOperation = operationService.save(operation);
            System.out.println("Создана операция с ID: " + savedOperation.getOperations_Id());

            boolean paymentSuccess = emulatePaymentProcessing();

            if (paymentSuccess) {
                savedOperation.setStatus("SUCCESS");
                operationService.save(savedOperation);

                // результат платежа для отдельной страницы
                session.setAttribute("paymentResultStatus", "success");
                session.setAttribute("paymentResultMessage", "Платёж успешно обработан");
                session.setAttribute("paymentResultOperationId", savedOperation.getOperations_Id());

                response.put("paymentSuccess", true);
            } else {
                savedOperation.setStatus("FAILED");
                operationService.save(savedOperation);

                session.setAttribute("paymentResultStatus", "error");
                session.setAttribute("paymentResultMessage", "Ошибка обработки платежа");
                session.setAttribute("paymentResultOperationId", savedOperation.getOperations_Id());

                response.put("paymentSuccess", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("paymentResultStatus", "error");
            session.setAttribute("paymentResultMessage", "Внутренняя ошибка сервера при обработке платежа");
            session.setAttribute("paymentResultOperationId", null);

            response.put("paymentSuccess", false);
        } finally {
            // одноразовый код и данные формы больше не нужны
            session.removeAttribute("otp");
            session.removeAttribute("surname");
            session.removeAttribute("nameUser");
            session.removeAttribute("patronymic");
            session.removeAttribute("amount");
            session.removeAttribute("purpose");
            session.removeAttribute("cardNumber");
            session.removeAttribute("email");
        }

        return response;
    }

    @GetMapping("/result")
    public String showPaymentResult(HttpSession session, Model model) {
        String status  = (String) session.getAttribute("paymentResultStatus");
        String message = (String) session.getAttribute("paymentResultMessage");
        Object opIdObj = session.getAttribute("paymentResultOperationId");

        if (status == null || message == null) {
            // Если нет результата в сессии – просто отправляем на форму
            return "redirect:/payment-form";
        }

        model.addAttribute("paymentStatus", status);
        model.addAttribute("paymentMessage", message);
        model.addAttribute("paymentOperationId", opIdObj);

        // очищаем, чтобы при F5 не дёргалось повторно
        session.removeAttribute("paymentResultStatus");
        session.removeAttribute("paymentResultMessage");
        session.removeAttribute("paymentResultOperationId");

        return "payment-result"; // новый шаблон
    }


    // ====== 5. Старый процессинг (если где-то ещё используется) ======
    // Можно оставить на всякий случай, он никому не мешает.
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
            Operation operation = new Operation();
            operation.setSurname(surname);
            operation.setName_user(nameUser);
            operation.setPatronymic(patronymic);
            operation.setAmount(new BigDecimal(amount));
            operation.setPurpose(purpose);
            operation.setCard_number(cardNumber);
            operation.setStatus("PENDING");
            operation.setCreated_at(LocalDateTime.now());

            Operation savedOperation = operationService.save(operation);
            System.out.println("Создана операция с ID: " + savedOperation.getOperations_Id());

            boolean paymentSuccess = emulatePaymentProcessing();

            Map<String, String> resp = new HashMap<>();

            if (paymentSuccess) {
                savedOperation.setStatus("SUCCESS");
                operationService.save(savedOperation);

                resp.put("status", "success");
                resp.put("message", "Платеж успешно обработан");
                resp.put("operationId", savedOperation.getOperations_Id().toString());
                return ResponseEntity.ok(resp);
            } else {
                savedOperation.setStatus("FAILED");
                operationService.save(savedOperation);

                resp.put("status", "error");
                resp.put("message", "Ошибка обработки платежа");
                return ResponseEntity.badRequest().body(resp);
            }

        } catch (Exception e) {
            e.printStackTrace();

            Map<String, String> resp = new HashMap<>();
            resp.put("status", "error");
            resp.put("message", "Ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    // ====== Вспомогательный метод эмуляции платежа ======
    private boolean emulatePaymentProcessing() {
        Random random = new Random();
        return random.nextInt(100) < 80; // 80% "успех"
    }
}
