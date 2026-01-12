package com.generator.qr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FakeBankService {

    private static final Logger log = LoggerFactory.getLogger(FakeBankService.class);

    private final PaymentService paymentService;

    public FakeBankService(@Lazy PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Async
    public void processPaymentAsync(String idempotencyKey, String txnId) {
        log.info("FakeBankService: Processing payment async for txnId={}", txnId);

        try {
            Thread.sleep(3000); // simulate delay
        } catch (InterruptedException e) {
            log.error("FakeBankService: Interrupted while processing txnId={}", txnId, e);
            Thread.currentThread().interrupt();
            return;
        }

        // Generate fake UTR
        String utr = "UTR-" + UUID.randomUUID();
        log.info("FakeBankService: Payment successful for txnId={}, generated UTR={}", txnId, utr);

        // Mark transaction success
        paymentService.markSuccess(idempotencyKey, utr);
    }
}
