package com.example.Payment.Controller;

import com.example.Payment.Dto.Mapping.OperationMapper;
import com.example.Payment.Dto.OperationCreateRequestDTO;
import com.example.Payment.Service.OperationService;
import com.example.Payment.Service.OtpEmailService;
import com.example.Payment.Service.ReceiptEmailService;
import com.example.Payment.Tables.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/payment-form")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class PaymentFormController {

    private static final DateTimeFormatter CHECK_DT_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Конфигурация для шлюза
    @Value("${payment.gateway.url:http://localhost:8081/ajax/mock-payment/verify-otp}")
    private String gatewayUrl;

    @Value("${payment.gateway.mock.enabled:true}")
    private boolean mockGatewayEnabled;

    @Autowired
    private OtpEmailService otpEmailService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private ReceiptEmailService receiptEmailService;

    @Autowired
    private OperationMapper operationMapper;

    // Инструмент для отправки запросов к внешнему шлюзу
    private final RestTemplate restTemplate = new RestTemplate();

    // ====== 1. ВСПОМОГАТЕЛЬНЫЙ МЕТОД: АЛГОРИТМ ЛУНА ======
    private boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }

        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");

        if (cleanNumber.length() < 13 || cleanNumber.length() > 19) {
            return false;
        }

        int sum = 0;
        boolean doubleDigit = false;

        for (int i = cleanNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleanNumber.charAt(i));

            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return (sum % 10 == 0);
    }

    // ====== 2. МЕТОДЫ ДЛЯ РАБОТЫ С ПЛАТЕЖНЫМ ШЛЮЗОМ ======

    /**
     * Метод для обращения к внешнему платежному шлюзу
     */
    private PaymentResult callExternalPaymentGateway(String cardholderName, String cardNumber, BigDecimal amount, String cvv, String expiryDate) {
        Map<String, Object> request = new HashMap<>();
        request.put("cardholderName", cardholderName);
        request.put("cardNumber", cardNumber.replaceAll("\\s+", ""));
        request.put("amount", amount);
        request.put("cvv",cvv);
        request.put("expiryDate",expiryDate);
        request.put("currency", "RUB");
        request.put("timestamp", LocalDateTime.now().toString());

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    gatewayUrl,
                    request,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String status = (String) body.get("status");
                String message = (String) body.get("message");
                String errorCode = (String) body.get("errorCode");
                String errorDetails = (String) body.get("errorDetails");
                String gatewayResponse = (String) body.get("gatewayResponse");

                return new PaymentResult(
                        "SUCCESS".equalsIgnoreCase(status),
                        message,
                        errorCode,
                        errorDetails,
                        gatewayResponse
                );
            }
        } catch (HttpClientErrorException e) {
            return new PaymentResult(
                    false,
                    "Клиентская ошибка: " + e.getMessage(),
                    "HTTP_CLIENT_ERROR",
                    e.getResponseBodyAsString(),
                    null
            );
        } catch (HttpServerErrorException e) {
            return new PaymentResult(
                    false,
                    "Серверная ошибка шлюза: " + e.getMessage(),
                    "HTTP_SERVER_ERROR",
                    e.getResponseBodyAsString(),
                    null
            );
        } catch (Exception e) {
            return new PaymentResult(
                    false,
                    "Ошибка подключения: " + e.getMessage(),
                    "CONNECTION_ERROR",
                    e.getMessage(),
                    null
            );
        }

        return new PaymentResult(
                false,
                "Неизвестная ошибка",
                "UNKNOWN_ERROR",
                null,
                null
        );
    }

    // Вспомогательный класс для результата
    private class PaymentResult {
        private boolean success;
        private String message;
        private String errorCode;
        private String errorDetails;
        private  String gatewayResponse;

        public PaymentResult(boolean success, String message, String errorCode, String errorDetails, String gatewayResponse) {
            this.success = success;
            this.message = message;
            this.errorCode = errorCode;
            this.errorDetails = errorDetails;
            this.gatewayResponse = gatewayResponse;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
        public String getErrorDetails() { return errorDetails; }

        public String getGatewayResponse() {
            return gatewayResponse;
        }
    }

    /**
     * Мок-реализация платежного шлюза для тестирования
     */
    private boolean callMockPaymentGateway(String cardholderName, String cardNumber, BigDecimal amount) {
        if (cardNumber != null && cardNumber.replaceAll("\\s+", "").startsWith("555")) {
            return false; // Тестовый отказ
        }

        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            return false;
        }

        Random random = new Random();
        if (random.nextDouble() < 0.05) {
            return false;
        }

        return true;
    }

    /**
     * Универсальный метод для обработки платежа через шлюз
     */
    private PaymentResult processPaymentThroughGateway(String cardholderName, String cardNumber, BigDecimal amount,String cvv,String expiryDate) {
        System.out.println("🔧 Вызов processPaymentThroughGateway");

        if (mockGatewayEnabled) {
            System.out.println("🔧 Используется ЛОКАЛЬНЫЙ mock шлюз");
            boolean success = callMockPaymentGateway(cardholderName, cardNumber, amount);
            return new PaymentResult(
                    success,
                    success ? "Платеж успешен (mock)" : "Платеж отклонен (mock)",
                    success ? null : "MOCK_ERROR",
                    success ? null : "Тестовый отказ от mock шлюза",
                    success ? null : "Отказ"
            );
        } else {
            System.out.println("🔧 Используется ВНЕШНИЙ шлюз: " + gatewayUrl);
            return callExternalPaymentGateway(cardholderName, cardNumber, amount,cvv,expiryDate);
        }
    }

    // ====== 3. ОСНОВНЫЕ МЕТОДЫ КОНТРОЛЛЕРА ======

    @GetMapping
    public String showPaymentForm(@RequestParam(required = false) String paymentId,
                                  Model model) {

        if (paymentId == null || paymentId.isEmpty()) {
            paymentId = "demo-" + System.currentTimeMillis();
        }

        model.addAttribute("paymentId", paymentId);
        model.addAttribute("amount", "1500.00");
        model.addAttribute("purpose", "Оплата через платежный шлюз");
        model.addAttribute("mockGatewayEnabled", mockGatewayEnabled);

        return "payment-form";
    }

    @GetMapping("/demo")
    public String showDemoForm(
            @RequestParam(required = false) Map<String, String> params,
            Model model) {

        String paymentId = params.getOrDefault("paymentId", "demo-" + System.currentTimeMillis());
        String amount = params.getOrDefault("amount", "1500.00");
        String product = params.getOrDefault("product", "Оплата услуги");
        String orderId = params.getOrDefault("order_id", "ORDER-" + System.currentTimeMillis());
        String returnUrl = params.getOrDefault("return_url", "");
        String email = params.getOrDefault("email", "");



        model.addAttribute("paymentId", paymentId);
        model.addAttribute("amount", amount);
        model.addAttribute("purpose", product);
        model.addAttribute("order_id", orderId);
        model.addAttribute("return_url", returnUrl);
        model.addAttribute("email", email);
        model.addAttribute("mockGatewayEnabled", mockGatewayEnabled);

        System.out.println("📋 Demo endpoint вызван с параметрами:");
        System.out.println("  - amount: " + amount);
        System.out.println("  - product: " + product);
        System.out.println("  - order_id: " + orderId);
        System.out.println("  - return_url: " + returnUrl);
        System.out.println("  - email: " + email);

        return "payment-form";
    }

    @PostMapping("/otp")
    public String handlePaymentFormAndShowOtp(
            @RequestParam String cardholderName,
            @RequestParam String amount,
            @RequestParam String purpose,
            @RequestParam String cardNumber,
            @RequestParam String email,
            @RequestParam String cvv,
            @RequestParam String expiryDate,
            HttpSession session,
            Model model) {

        // ============ ВАЛИДАЦИЯ ============

        // Проверка на кириллицу в имени
        if (cardholderName != null && cardholderName.matches(".*[а-яА-ЯёЁ].*")) {
            model.addAttribute("error", "Имя владельца карты должно быть на латинице");
            return populateModelWithFormData(model, cardholderName, amount, purpose, cardNumber, email, cvv, expiryDate);
        }

        // Проверка формата имени
        if (cardholderName != null) {
            String[] nameParts = cardholderName.trim().split("\\s+");
            if (nameParts.length < 2) {
                model.addAttribute("error", "Введите имя и фамилию (например: IVANOV IVAN)");
                return populateModelWithFormData(model, cardholderName, amount, purpose, cardNumber, email,cvv, expiryDate);
            }

            if (!cardholderName.matches("^[A-Za-z\\s\\-'’.]+$")) {
                model.addAttribute("error", "Имя может содержать только латинские буквы, пробелы, дефисы и апострофы");
                return populateModelWithFormData(model, cardholderName, amount, purpose, cardNumber, email,cvv, expiryDate);
            }
        }

        // Валидация номера карты алгоритмом Луна
        if (cardNumber != null && !isValidLuhn(cardNumber)) {
            model.addAttribute("error", "Неверный номер карты. Проверьте правильность ввода.");
            return populateModelWithFormData(model, cardholderName, amount, purpose, cardNumber, email,cvv, expiryDate);
        }

        // ============ СОХРАНЕНИЕ ДАННЫХ ============

        session.setAttribute("cardholderName", cardholderName);
        session.setAttribute("amount", amount);
        session.setAttribute("purpose", purpose);
        session.setAttribute("cardNumber", cardNumber);
        session.setAttribute("email", email);
        session.setAttribute("cvv",cvv);
        session.setAttribute("expiryDate",expiryDate);

        // ============ ГЕНЕРАЦИЯ И ОТПРАВКА OTP ============

        int code = ThreadLocalRandom.current().nextInt(1000, 10000);
        session.setAttribute("otp", code);
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
            return populateModelWithFormData(model, cardholderName, amount, purpose, cardNumber, email,cvv, expiryDate);
        }

        System.out.println("Код подтверждения для пользователя " + email + ": " + code);
        return "SMSVerification";
    }

    @PostMapping("/verify-otp")
    @ResponseBody
    public Map<String, Object> verifyOtp(@RequestBody Map<String, String> body,
                                         HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 1. Проверка OTP
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

        response.put("otpValid", true);

        try {
            // 2. Получение данных из сессии
            String cardholderName = (String) session.getAttribute("cardholderName");
            String amountStr = (String) session.getAttribute("amount");
            String purpose = (String) session.getAttribute("purpose");
            String cardNumber = (String) session.getAttribute("cardNumber");
            String email = (String) session.getAttribute("email");
            String cvv = (String) session.getAttribute("cvv");
            String expiryDate = (String) session.getAttribute("expiryDate");

            // Сохраняем данные для возврата на внешний сайт
            session.setAttribute("paymentAmount", amountStr);
            session.setAttribute("paymentPurpose", purpose);

            // Получаем данные о внешнем сайте из параметров URL
            String returnUrl = (String) session.getAttribute("externalReturnUrl");
            String externalOrderId = (String) session.getAttribute("externalOrderId");
            String externalEmail = (String) session.getAttribute("externalEmail");

            // Если их нет в сессии, возможно они пришли из параметров формы
            if (returnUrl == null) {
                // Пытаемся получить из других источников
                returnUrl = (String) session.getAttribute("return_url");
            }

            // Сохраняем для страницы результата
            if (returnUrl != null) {
                session.setAttribute("returnUrl", returnUrl);
            }
            if (externalOrderId != null) {
                session.setAttribute("externalOrderId", externalOrderId);
            }
            if (externalEmail != null) {
                session.setAttribute("externalEmail", externalEmail);
            }

            // 3. Создание операции со статусом PENDING
            Operation operation = new Operation();
            operation.setCardholderName(cardholderName);
            operation.setAmount(new BigDecimal(amountStr));
            operation.setPurpose(purpose);
            operation.setCard_number(cardNumber);
            operation.setStatus("PENDING");
            operation.setCreated_at(LocalDateTime.now());

            Operation savedOperation = operationService.save(operation);
            System.out.println("Создана операция с ID: " + savedOperation.getOperations_Id());

            // 4. ОБРАЩЕНИЕ К ПЛАТЕЖНОМУ ШЛЮЗУ
            PaymentResult paymentResult = processPaymentThroughGateway(cardholderName, cardNumber, new BigDecimal(amountStr),cvv, expiryDate);

            if (paymentResult.isSuccess()) {
                savedOperation.setStatus("SUCCESS");
                savedOperation.setGatewayMessage(paymentResult.getMessage());
                savedOperation.setGatewayResponse(paymentResult.getGatewayResponse());// Сохраняем сообщение
                operationService.save(savedOperation);

                // Сохранение результата для страницы
                session.setAttribute("paymentResultStatus", "success");
                session.setAttribute("paymentResultMessage", paymentResult.getMessage());
                session.setAttribute("paymentResultOperationId", savedOperation.getOperations_Id());

                // Создание чека
                ReceiptView receipt = new ReceiptView(
                        "DemoShop",
                        randomInn10(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                        "Приход",
                        (purpose != null && !purpose.isBlank()) ? ("Оплата услуги: " + purpose) : "Оплата услуги DemoShop",
                        new BigDecimal(amountStr),
                        "https://nalog.gov.ru/",
                        "https://www.nalog.gov.ru/rn77/about_fts/docs/3909988/",
                        ""
                );
                session.setAttribute("paymentResultReceipt", receipt);

                // Отправка чека на email
                try {
                    receiptEmailService.sendReceipt(email, receipt, savedOperation.getOperations_Id());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    session.setAttribute("paymentResultMailWarning",
                            "Чек не удалось отправить на email: " + email + ". Причина: "
                                    + ex.getClass().getSimpleName() + " — " + ex.getMessage());
                }

                response.put("paymentSuccess", true);
                response.put("message", paymentResult.getMessage());
                response.put("operationId", savedOperation.getOperations_Id());
                response.put("amount", amountStr);
                response.put("product", purpose);
            } else {
                //  ВАЖНО: Сохраняем детали ошибки из шлюза!
                savedOperation.setStatus("FAILED");
                savedOperation.setGatewayMessage(paymentResult.getMessage());
                savedOperation.setErrorCode(paymentResult.getErrorCode());
                savedOperation.setErrorDetails(paymentResult.getErrorDetails());
                savedOperation.setGatewayResponse(paymentResult.getGatewayResponse());
                operationService.save(savedOperation);

                session.setAttribute("paymentResultStatus", "error");
                session.setAttribute("paymentResultMessage", paymentResult.getMessage()); // Сообщение из шлюза!
                session.setAttribute("paymentResultErrorCode", paymentResult.getErrorCode());
                session.setAttribute("paymentResultErrorDetails", paymentResult.getErrorDetails());
                session.setAttribute("paymentResultOperationId", savedOperation.getOperations_Id());

                response.put("paymentSuccess", false);
                response.put("message", paymentResult.getMessage()); // Конкретная причина!
                response.put("errorCode", paymentResult.getErrorCode());
                response.put("errorDetails", paymentResult.getErrorDetails());
                response.put("operationId", savedOperation.getOperations_Id());
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("paymentResultStatus", "error");
            session.setAttribute("paymentResultMessage", "Внутренняя ошибка сервера при обработке платежа");
            session.setAttribute("paymentResultOperationId", null);

            response.put("paymentSuccess", false);
            response.put("message", "Ошибка при обработке платежа");
        } finally {
            // Очистка сессии
            session.removeAttribute("otp");
            session.removeAttribute("cardholderName");
            session.removeAttribute("amount");
            session.removeAttribute("purpose");
            session.removeAttribute("cardNumber");
            session.removeAttribute("email");
        }

        return response;
    }

    @GetMapping("/result")
    public String showPaymentResult(HttpSession session, Model model) {
        String status = (String) session.getAttribute("paymentResultStatus");
        String message = (String) session.getAttribute("paymentResultMessage");
        Object opIdObj = session.getAttribute("paymentResultOperationId");

        // Получаем данные о внешнем сайте из сессии
        String returnUrl = (String) session.getAttribute("externalReturnUrl");
        String externalOrderId = (String) session.getAttribute("externalOrderId");
        String externalEmail = (String) session.getAttribute("externalEmail");
        String paymentAmount = (String) session.getAttribute("paymentAmount");
        String paymentPurpose = (String) session.getAttribute("paymentPurpose");

        // =============================================
        // ОТЛАДОЧНАЯ ИНФОРМАЦИЯ
        // =============================================
        System.out.println("🔍 ===== ДЕБАГ: Метод /result вызван =====");
        System.out.println("📦 Данные из сессии:");
        System.out.println("  - paymentResultStatus: " + status);
        System.out.println("  - paymentResultMessage: " + message);
        System.out.println("  - paymentResultOperationId: " + opIdObj);
        System.out.println("  - externalReturnUrl: " + returnUrl);
        System.out.println("  - externalOrderId: " + externalOrderId);
        System.out.println("  - externalEmail: " + externalEmail);
        System.out.println("  - paymentAmount: " + paymentAmount);
        System.out.println("  - paymentPurpose: " + paymentPurpose);

        // Проверяем все возможные места, где мог сохраниться returnUrl
        System.out.println("🔎 Проверяю все возможные источники returnUrl:");
        System.out.println("  - externalReturnUrl: " + session.getAttribute("externalReturnUrl"));
        System.out.println("  - return_url: " + session.getAttribute("return_url"));
        System.out.println("  - returnUrl: " + session.getAttribute("returnUrl"));
        // =============================================

        if (status == null || message == null) {
            System.out.println("❌ Статус или сообщение пустые, редирект на /payment-form");
            return "redirect:/payment-form";
        }

        // =============================================
        // ВАЖНО: Если returnUrl пустой, используем хардкод для внешнего сайта
        // =============================================
        if (returnUrl == null || returnUrl.trim().isEmpty()) {
            System.out.println("⚠ ВНИМАНИЕ: returnUrl пустой! Ищу альтернативные источники...");

            // Пробуем найти returnUrl в других атрибутах сессии
            returnUrl = (String) session.getAttribute("return_url");
            if (returnUrl == null || returnUrl.trim().isEmpty()) {
                returnUrl = (String) session.getAttribute("returnUrl");
            }

            // Если всё равно пусто, используем хардкод
            if (returnUrl == null || returnUrl.trim().isEmpty()) {
                System.out.println("⚠ ВНИМАНИЕ: returnUrl не найден ни в одном источнике!");
                System.out.println("🛠 Использую хардкод для внешнего сайта");

                // ХАРДКОД для внешнего сайта
                returnUrl = "http://localhost:3000/demoshop.html";

                // Также хардкодим другие данные если они пустые
                if (externalOrderId == null || externalOrderId.trim().isEmpty()) {
                    externalOrderId = "ORDER-" + System.currentTimeMillis();
                    System.out.println("🛠 Генерирую новый externalOrderId: " + externalOrderId);
                }

                if (paymentAmount == null || paymentAmount.trim().isEmpty()) {
                    paymentAmount = "25000";
                    System.out.println("🛠 Устанавливаю paymentAmount по умолчанию: " + paymentAmount);
                }

                if (paymentPurpose == null || paymentPurpose.trim().isEmpty()) {
                    paymentPurpose = "Смартфон Premium Pro";
                    System.out.println("🛠 Устанавливаю paymentPurpose по умолчанию: " + paymentPurpose);
                }
            } else {
                System.out.println("✅ Нашел returnUrl в альтернативном источнике: " + returnUrl);
            }
        } else {
            System.out.println("✅ returnUrl найден в externalReturnUrl: " + returnUrl);
        }
        // =============================================

        model.addAttribute("paymentStatus", status);
        model.addAttribute("paymentMessage", message);
        model.addAttribute("paymentOperationId", opIdObj);
        model.addAttribute("paymentAmount", paymentAmount);
        model.addAttribute("paymentPurpose", paymentPurpose);
        model.addAttribute("mockGatewayEnabled", mockGatewayEnabled);
        model.addAttribute("paymentResultErrorCode", session.getAttribute("paymentResultErrorCode"));
        model.addAttribute("paymentResultErrorDetails", session.getAttribute("paymentResultErrorDetails"));

        // Передаем данные о внешнем сайте (гарантированно не пустые)
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("externalOrderId", externalOrderId);
        model.addAttribute("externalEmail", externalEmail);

        // =============================================
        // Дополнительная отладочная информация в модель
        // =============================================
        model.addAttribute("debugInfo", "returnUrl: " + returnUrl +
                ", orderId: " + externalOrderId +
                ", amount: " + paymentAmount);
        // =============================================

        if ("success".equals(status)) {
            ReceiptView receipt = (ReceiptView) session.getAttribute("paymentResultReceipt");
            model.addAttribute("receipt", receipt);

            String mailWarning = (String) session.getAttribute("paymentResultMailWarning");
            model.addAttribute("mailWarning", mailWarning);
        }

        // =============================================
        // ВАЖНОЕ ИЗМЕНЕНИЕ: Очистка сессии
        // =============================================
        // Очищаем только платежные данные, данные внешнего сайта НЕ очищаем
        // Они могут понадобиться, если пользователь обновит страницу
        System.out.println("🧹 Очищаю сессию (кроме данных внешнего сайта)...");

        session.removeAttribute("paymentResultStatus");
        session.removeAttribute("paymentResultMessage");
        session.removeAttribute("paymentResultOperationId");
        session.removeAttribute("paymentResultReceipt");
        session.removeAttribute("paymentResultMailWarning");
        session.removeAttribute("paymentResultErrorCode");
        session.removeAttribute("paymentResultErrorDetails");

        // НЕ очищаем эти данные - они нужны для возврата:
        // - externalReturnUrl
        // - externalOrderId
        // - externalEmail
        // - paymentAmount
        // - paymentPurpose

        System.out.println("✅ Данные сохранены в сессии для возможного повторного использования:");
        System.out.println("  - externalReturnUrl остается: " + session.getAttribute("externalReturnUrl"));
        System.out.println("  - externalOrderId остается: " + session.getAttribute("externalOrderId"));
        System.out.println("  - paymentAmount остается: " + session.getAttribute("paymentAmount"));
        // =============================================

        return "payment-result";
    }

    // ====== 4. REST API ДЛЯ ВНЕШНИХ САЙТОВ ======

    /**
     * API: Инициализация платежа
     * POST /payment-form/api/init
     */
    @PostMapping("/api/init")
    @ResponseBody
    public ResponseEntity<?> apiInitPayment(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("🔧 API: Инициализация платежа для внешнего сайта");
            System.out.println("  - Данные: " + request);

            BigDecimal amount = null;
            if (request.get("amount") instanceof Integer) {
                amount = new BigDecimal((Integer) request.get("amount"));
            } else if (request.get("amount") instanceof Double) {
                amount = BigDecimal.valueOf((Double) request.get("amount"));
            } else if (request.get("amount") instanceof String) {
                amount = new BigDecimal((String) request.get("amount"));
            }

            String product = (String) request.get("product");
            String orderId = (String) request.get("orderId");
            String returnUrl = (String) request.get("returnUrl");
            String email = (String) request.get("email");

            // Валидация
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Некорректная сумма платежа"
                ));
            }

            if (product == null || product.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Название товара обязательно"
                ));
            }

            // Генерация уникального paymentId
            String paymentId = "EXT-" + System.currentTimeMillis() + "-" +
                    ThreadLocalRandom.current().nextInt(1000, 9999);

            // Создаем операцию в статусе INITIATED
            Operation operation = new Operation();
            operation.setAmount(amount);
            operation.setPurpose(product);
            operation.setStatus("INITIATED");
//            operation.setExternalOrderId(orderId);
//            operation.setExternalReturnUrl(returnUrl);
//            operation.setExternalEmail(email);
            operation.setCreated_at(LocalDateTime.now());

            Operation savedOperation = operationService.save(operation);

            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("status", "INITIATED");
            response.put("paymentId", paymentId);
            response.put("operationId", savedOperation.getOperations_Id());
            response.put("amount", savedOperation.getAmount());
            response.put("product", savedOperation.getPurpose());
            response.put("message", "Платеж инициализирован");
            response.put("nextStepUrl", "/payment-form/demo?paymentId=" + paymentId +
                    "&amount=" + amount +
                    "&product=" + (product != null ? java.net.URLEncoder.encode(product, "UTF-8") : "") +
                    "&order_id=" + (orderId != null ? orderId : "") +
                    "&return_url=" + (returnUrl != null ? java.net.URLEncoder.encode(returnUrl, "UTF-8") : "") +
                    "&email=" + (email != null ? java.net.URLEncoder.encode(email, "UTF-8") : ""));
            response.put("timestamp", LocalDateTime.now());

            System.out.println("✅ API платеж инициализирован. Payment ID: " + paymentId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Ошибка при инициализации платежа: " + e.getMessage()
            ));
        }
    }

    /**
     * API: Прямой платеж без OTP (для простых интеграций)
     * POST /payment-form/api/process
     */
    @PostMapping("/api/process")
    @ResponseBody
    public ResponseEntity<?> apiProcessPayment(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("🔧 API: Прямой платеж для внешнего сайта");
            System.out.println("  - Данные: " + request);

            // Извлекаем данные
            String cardholderName = (String) request.get("cardholderName");
            String cardNumber = (String) request.get("cardNumber");
            String cvv = (String) request.get("cvv");
            String expiryDate = (String) request.get("expiryDate");
            String email = (String) request.get("email");
            String product = (String) request.get("product");
            String orderId = (String) request.get("orderId");

            BigDecimal amount = null;
            if (request.get("amount") instanceof Integer) {
                amount = new BigDecimal((Integer) request.get("amount"));
            } else if (request.get("amount") instanceof Double) {
                amount = BigDecimal.valueOf((Double) request.get("amount"));
            } else if (request.get("amount") instanceof String) {
                amount = new BigDecimal((String) request.get("amount"));
            }

            // Валидация
            if (cardholderName == null || cardholderName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Имя держателя карты обязательно"
                ));
            }

            if (cardNumber == null || !isValidLuhn(cardNumber)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Неверный номер карты"
                ));
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Некорректная сумма платежа"
                ));
            }

            // Создание операции
            Operation operation = new Operation();
            operation.setCardholderName(cardholderName);
            operation.setCard_number(cardNumber);
            operation.setAmount(amount);
            operation.setPurpose(product != null ? product : "Оплата услуги");
            operation.setStatus("PROCESSING");
//            operation.setExternalEmail(email);
//            operation.setExternalOrderId(orderId);
            operation.setCreated_at(LocalDateTime.now());

            Operation savedOperation = operationService.save(operation);

            // Обработка платежа через шлюз
            PaymentResult paymentResult = processPaymentThroughGateway(
                    cardholderName, cardNumber, amount, cvv, expiryDate
            );

            Map<String, Object> response = new HashMap<>();

            if (paymentResult.isSuccess()) {
                savedOperation.setStatus("SUCCESS");
                savedOperation.setGatewayMessage(paymentResult.getMessage());
                savedOperation.setGatewayResponse(paymentResult.getGatewayResponse());
                operationService.save(savedOperation);

                // Отправка чека на email
                if (email != null && !email.isBlank()) {
                    try {
                        ReceiptView receipt = new ReceiptView(
                                "DemoShop",
                                randomInn10(),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                                "Приход",
                                product != null ? ("Оплата: " + product) : "Оплата услуги",
                                amount,
                                "https://nalog.gov.ru/",
                                "https://www.nalog.gov.ru/rn77/about_fts/docs/3909988/",
                                ""
                        );
                        receiptEmailService.sendReceipt(email, receipt, savedOperation.getOperations_Id());
                    } catch (Exception ex) {
                        System.err.println("Ошибка отправки чека: " + ex.getMessage());
                    }
                }

                response.put("status", "SUCCESS");
                response.put("operationId", savedOperation.getOperations_Id());
                response.put("message", "Платеж успешно выполнен");
                response.put("amount", savedOperation.getAmount());
                response.put("product", savedOperation.getPurpose());
                response.put("receiptSent", email != null && !email.isBlank());

            } else {
                savedOperation.setStatus("FAILED");
                savedOperation.setGatewayMessage(paymentResult.getMessage());
                savedOperation.setErrorCode(paymentResult.getErrorCode());
                savedOperation.setErrorDetails(paymentResult.getErrorDetails());
                savedOperation.setGatewayResponse(paymentResult.getGatewayResponse());
                operationService.save(savedOperation);

                response.put("status", "FAILED");
                response.put("operationId", savedOperation.getOperations_Id());
                response.put("message", paymentResult.getMessage());
                response.put("errorCode", paymentResult.getErrorCode());
                response.put("errorDetails", paymentResult.getErrorDetails());
            }

            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Ошибка при обработке платежа: " + e.getMessage()
            ));
        }
    }

    /**
     * API: Проверка статуса операции
     * GET /payment-form/api/operations/{id}
     */
//    @GetMapping("/api/operations/{id}")
//    @ResponseBody
//    public ResponseEntity<?> apiGetOperationStatus(@PathVariable Long id) {
//        try {
//            Operation operation = operationService.findById(id);
//
//            if (operation == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                        "status", "error",
//                        "message", "Операция не найдена"
//                ));
//            }
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("operationId", operation.getOperations_Id());
//            response.put("status", operation.getStatus());
//            response.put("message", operation.getGatewayMessage());
//            response.put("errorCode", operation.getErrorCode());
//            response.put("errorDetails", operation.getErrorDetails());
//            response.put("amount", operation.getAmount());
//            response.put("cardholderName", operation.getCardholderName());
//            response.put("product", operation.getPurpose());
//            response.put("createdAt", operation.getCreated_at());
//            response.put("timestamp", LocalDateTime.now());
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                    "status", "error",
//                    "message", "Ошибка при получении статуса операции: " + e.getMessage()
//            ));
//        }
//    }

    // ====== 5. ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ======

    private String populateModelWithFormData(Model model,
                                             String cardholderName,
                                             String amount,
                                             String purpose,
                                             String cardNumber,
                                             String email,
                                             String cvv,
                                             String expiryDate) {
        model.addAttribute("cardholderName", cardholderName);
        model.addAttribute("amount", amount);
        model.addAttribute("purpose", purpose);
        model.addAttribute("cardNumber", cardNumber);
        model.addAttribute("email", email);
        model.addAttribute("cvv", cvv);
        model.addAttribute("expiryDate", expiryDate);
        model.addAttribute("mockGatewayEnabled", mockGatewayEnabled);
        return "payment-form";
    }

    private static String randomInn10() {
        StringBuilder sb = new StringBuilder();
        sb.append(ThreadLocalRandom.current().nextInt(1, 10));
        for (int i = 0; i < 9; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
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
}