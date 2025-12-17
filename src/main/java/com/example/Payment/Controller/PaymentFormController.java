package com.example.Payment.Controller;

import com.example.Payment.Dto.Mapping.OperationMapper;
import com.example.Payment.Dto.OperationCreateRequestDTO;
import com.example.Payment.Service.OperationService;
import com.example.Payment.Tables.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;





@Controller
@RequestMapping("/payment-form")
public class PaymentFormController {

    private static final DateTimeFormatter CHECK_DT_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    @Autowired
    private com.example.Payment.Service.OtpEmailService otpEmailService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private com.example.Payment.Service.ReceiptEmailService receiptEmailService;

    @Autowired
    private OperationMapper operationMapper;

    // ====== ВСПОМОГАТЕЛЬНЫЙ МЕТОД: АЛГОРИТМ ЛУНА ======
    private boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }

        // Удаляем все нецифровые символы
        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");

        // Проверяем длину (стандартные карты: 13-19 цифр)
        if (cleanNumber.length() < 13 || cleanNumber.length() > 19) {
            return false;
        }

        int sum = 0;
        boolean doubleDigit = false;

        // Идем справа налево
        for (int i = cleanNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleanNumber.charAt(i));

            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9; // Эквивалентно сложению цифр (16 → 1+6=7, но 16-9=7)
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return (sum % 10 == 0);
    }

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
            @RequestParam String cardholderName,
            @RequestParam String amount,
            @RequestParam String purpose,
            @RequestParam String cardNumber,
            @RequestParam String email,
            HttpSession session,
            Model model) {

        // ============ ВАЛИДАЦИЯ КИРИЛЛИЦЫ ============
        if (cardholderName != null && cardholderName.matches(".*[а-яА-ЯёЁ].*")) {
            model.addAttribute("error", "Имя владельца карты должно быть написано латинскими буквами");
            model.addAttribute("cardholderName", cardholderName);
            model.addAttribute("amount", amount);
            model.addAttribute("purpose", purpose);
            model.addAttribute("cardNumber", cardNumber);
            model.addAttribute("email", email);
            return "payment-form"; // возвращаем обратно на форму с ошибкой
        }

        // ============ ДОПОЛНИТЕЛЬНАЯ ВАЛИДАЦИЯ ============
        // Проверка формата имени (минимум 2 слова)
        if (cardholderName != null) {
            String[] nameParts = cardholderName.trim().split("\\s+");
            if (nameParts.length < 2) {
                model.addAttribute("error", "Введите имя и фамилию (например: IVANOV IVAN)");
                model.addAttribute("cardholderName", cardholderName);
                model.addAttribute("amount", amount);
                model.addAttribute("purpose", purpose);
                model.addAttribute("cardNumber", cardNumber);
                model.addAttribute("email", email);
                return "payment-form";
            }

            // Проверка на латинские буквы (более строгая)
            if (!cardholderName.matches("^[A-Za-z\\s\\-'’.]+$")) {
                model.addAttribute("error", "Имя может содержать только латинские буквы, пробелы, дефисы и апострофы");
                model.addAttribute("cardholderName", cardholderName);
                model.addAttribute("amount", amount);
                model.addAttribute("purpose", purpose);
                model.addAttribute("cardNumber", cardNumber);
                model.addAttribute("email", email);
                return "payment-form";
            }
        }

        // ============ ВАЛИДАЦИЯ НОМЕРА КАРТЫ (АЛГОРИТМ ЛУНА) ============
        if (cardNumber != null && !isValidLuhn(cardNumber)) {
            model.addAttribute("error", "Неверный номер карты. Проверьте правильность ввода.");
            model.addAttribute("cardholderName", cardholderName);
            model.addAttribute("amount", amount);
            model.addAttribute("purpose", purpose);
            model.addAttribute("cardNumber", cardNumber);
            model.addAttribute("email", email);
            return "payment-form";
        }
        // ============ КОНЕЦ ВАЛИДАЦИИ ============


        // сохраняем все данные формы в сессию
        session.setAttribute("cardholderName", cardholderName);
        session.setAttribute("amount", amount);
        session.setAttribute("purpose", purpose);
        session.setAttribute("cardNumber", cardNumber);
        session.setAttribute("email", email);

        // генерируем 4-значный код
        int code = ThreadLocalRandom.current().nextInt(1000, 10000);
        session.setAttribute("otp", code);
        // статус для UI
        session.setAttribute("otpMailStatus", "sending");
        session.setAttribute("otpMailError", null);
        try {
            otpEmailService.sendOtpAsync(email, code)
                    .thenRun(() -> session.setAttribute("otpMailStatus", "sent"))
                    .exceptionally(ex -> {
                        session.setAttribute("otpMailStatus", "error");
                        session.setAttribute("otpMailError", ex.getMessage());
                        return null;
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("error", "Не удалось отправить код подтверждения на email. Проверьте адрес или настройки почты.");
            model.addAttribute("cardholderName", cardholderName);
            model.addAttribute("amount", amount);
            model.addAttribute("purpose", purpose);
            model.addAttribute("cardNumber", cardNumber);
            model.addAttribute("email", email);
            return "payment-form";
        }

// показываем страницу ввода кода
        return "SMSVerification";

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
            String cardholderName    = (String) session.getAttribute("cardholderName");
            String amountStr  = (String) session.getAttribute("amount");
            String purpose    = (String) session.getAttribute("purpose");
            String cardNumber = (String) session.getAttribute("cardNumber");
            String email      = (String) session.getAttribute("email");

            // Финальная проверка алгоритмом Луна (на всякий случай)
            if (!isValidLuhn(cardNumber)) {
                response.put("paymentSuccess", false);
                response.put("message", "Неверный номер карты");
                return response;
            }

            Operation operation = new Operation();
            operation.setCardholderName(cardholderName);
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

                session.setAttribute("paymentResultAmount", amountStr);
                session.setAttribute("paymentResultPurpose", purpose);


                // ---- создаём чек ОДИН РАЗ (и для страницы, и для email)
                ReceiptView receipt = new ReceiptView(
                        "DemoShop",
                        randomInn10(),
                        LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                        "Приход",
                        (purpose != null && !purpose.isBlank()) ? ("Оплата услуги: " + purpose) : "Оплата услуги DemoShop",
                        new BigDecimal(amountStr),
                        "https://nalog.gov.ru/",
                        "https://www.nalog.gov.ru/rn77/about_fts/docs/3909988/", // 54-ФЗ на сайте ФНС
                        "" // qrUrl в шаблоне страницы не нужен, если используешь /qr.svg
                );
                session.setAttribute("paymentResultReceipt", receipt);

                // ---- отправляем письмо
                try {
                    receiptEmailService.sendReceipt(email, receipt, savedOperation.getOperations_Id());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // не валим оплату, просто фиксируем что email не ушёл
                    session.setAttribute("paymentResultMailWarning",
                            "Чек не удалось отправить на email: " + email + ". Причина: "
                                    + ex.getClass().getSimpleName() + " — " + ex.getMessage());
                }


                response.put("paymentSuccess", true);
            }
            else {
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

        if ("success".equals(status)) {
            ReceiptView receipt = (ReceiptView) session.getAttribute("paymentResultReceipt");
            model.addAttribute("receipt", receipt);

            String mailWarning = (String) session.getAttribute("paymentResultMailWarning");
            model.addAttribute("mailWarning", mailWarning);
        }



        // очищаем, чтобы при F5 не дёргалось повторно
        session.removeAttribute("paymentResultStatus");
        session.removeAttribute("paymentResultMessage");
        session.removeAttribute("paymentResultOperationId");
        session.removeAttribute("paymentResultAmount");
        session.removeAttribute("paymentResultPurpose");
        session.removeAttribute("paymentResultReceipt");
        session.removeAttribute("paymentResultMailWarning");



        return "payment-result"; // новый шаблон
    }


    // ====== 5. Старый процессинг (если где-то ещё используется) ======
    // Можно оставить на всякий случай, он никому не мешает.
    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<?> processPayment(
            @Valid @ModelAttribute OperationCreateRequestDTO operationCreateRequestDTO, // ← @Valid здесь!
            BindingResult bindingResult) { // ← BindingResult для ошибок

        // Проверяем ошибки валидации
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "validation_error");
            response.put("message", "Ошибки валидации");

            // Собираем все ошибки
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        // Дополнительная проверка алгоритмом Луна
//        if (!isValidLuhn(operationCreateRequestDTO.getCard_number())) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("status", "validation_error");
//            response.put("message", "Неверный номер карты");
//            response.put("errors", List.of("card_number: Неверный номер карты"));
//            return ResponseEntity.badRequest().body(response);
//        }

        try {

            Operation operation = operationMapper.toEntity(operationCreateRequestDTO);

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
    private static String randomInn10() {
        StringBuilder sb = new StringBuilder();
        sb.append(ThreadLocalRandom.current().nextInt(1, 10)); // первая цифра 1..9
        for (int i = 0; i < 9; i++) sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        return sb.toString();
    }

    public record ReceiptView(
            String sellerName,
            String sellerInn,
            String datetime,
            String paymentSign,
            String itemName,
            BigDecimal amount,
            String fnsUrl,
            String law54Url,
            String qrUrl
    ) {}
    // ====== Вспомогательный метод эмуляции платежа ======
    private boolean emulatePaymentProcessing() {
        Random random = new Random();
        return random.nextInt(100) < 80; // 80% "успех"
    }
}