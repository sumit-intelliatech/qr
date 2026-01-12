package com.generator.qr.controller;

import com.generator.qr.dto.PaymentRequest;
import com.generator.qr.entity.PaymentTransaction;
import com.generator.qr.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }


    @PostMapping("/confirm")
    public ResponseEntity<PaymentTransaction> confirmPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentRequest request) {

        return ResponseEntity.ok(
                service.confirmPayment(request, idempotencyKey)
        );
    }


}

