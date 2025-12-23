//package com.example.Payment.Service;
//
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(
//        name = "payment-gateway",
//        url = "${services.gateway.base-url}",
//        configuration = FeignConfig.class
//)
//public interface GatewayClientService {
//
//    @PostMapping("${services.gateway.endpoints.process-payment}")
//    GatewayPaymentResponse processPayment(@RequestBody GatewayPaymentRequest request);
//
//    // Можно добавить другие методы шлюза
//}
