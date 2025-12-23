//package com.example.Payment.Service;
//
//import com.example.Payment.Repository.OperationRepository;
//import com.example.Payment.Tables.Operation;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class PaymentService {
//
//    private final OperationRepository operationRepository;
//    private final GatewayClient gatewayClient; // Feign клиент к шлюзу
//
//    @Transactional
//    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
//        log.info("Processing API payment for card: {}",
//                maskCardNumber(request.getCardNumber()));
//
//        // 1. Создание операции
//        Operation operation = createOperation(request);
//
//        // 2. Вызов платежного шлюза
//        GatewayResponse gatewayResponse = callPaymentGateway(operation, request);
//
//        // 3. Обновление статуса
//        updateOperationStatus(operation, gatewayResponse);
//
//        // 4. Формирование ответа
//        return createResponse(operation, gatewayResponse);
//    }
//
//    private Operation createOperation(PaymentRequestDTO request) {
//        Operation operation = new Operation();
//        //operation.setExternalId(UUID.randomUUID().toString());
//        operation.setCard_number(request.getCard_number());
//        operation.setAmount(new BigDecimal(request.getAmount()));
//        operation.setCurrency(request.getCurrency());
//        operation.setStatus(OperationStatus.PENDING);
//        operation.setCreatedAt(LocalDateTime.now());
//        return operationRepository.save(operation);
//    }
//
//    private GatewayResponse callPaymentGateway(Operation operation, PaymentRequestDTO request) {
//        GatewayRequest gatewayRequest = GatewayRequest.builder()
//                .operationId(operation.getExternalId())
//                .cardNumber(request.getCardNumber())
//                .amount(request.getAmount())
//                .currency(request.getCurrency())
//                .build();
//
//        try {
//            return gatewayClient.processPayment(gatewayRequest);
//        } catch (Exception e) {
//            log.error("Gateway call failed: {}", e.getMessage());
//            return GatewayResponse.builder()
//                    .status("ERROR")
//                    .message("Gateway unavailable")
//                    .build();
//        }
//    }
//
//    private void updateOperationStatus(Operation operation, GatewayResponse response) {
//        if ("SUCCESS".equals(response.getStatus())) {
//            operation.setStatus(OperationStatus.SUCCESS);
//            operation.setGatewayTransactionId(response.getTransactionId());
//        } else {
//            operation.setStatus(OperationStatus.FAILED);
//            operation.setErrorMessage(response.getMessage());
//        }
//        operation.setProcessedAt(LocalDateTime.now());
//        operationRepository.save(operation);
//    }
//
//    private PaymentResponseDTO createResponse(Operation operation, GatewayResponse gatewayResponse) {
//        PaymentResponseDTO response = new PaymentResponseDTO();
//        response.setTransactionId(operation.getExternalId());
//        response.setOperationId(operation.getId().toString());
//        response.setStatus(gatewayResponse.getStatus());
//        response.setMessage(gatewayResponse.getMessage());
//        response.setGateway("ExternalGateway");
//        return response;
//    }
//
//    private String maskCardNumber(String cardNumber) {
//        if (cardNumber == null || cardNumber.length() < 8) return "****";
//        String clean = cardNumber.replaceAll("\\s+", "");
//        return clean.substring(0, 4) + " **** **** " + clean.substring(Math.max(clean.length() - 4, 0));
//    }
//}
