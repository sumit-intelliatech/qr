package com.generator.qr.service;

import com.generator.qr.dto.PaymentRequest;
import com.generator.qr.entity.PaymentStatus;
import com.generator.qr.entity.PaymentTransaction;
import com.generator.qr.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@EnableAsync
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository repository;
    private final FakeBankService fakeBankService;

    public PaymentService(PaymentRepository repository, @Lazy FakeBankService fakeBankService) {
        this.repository = repository;
        this.fakeBankService = fakeBankService;
    }

    @Transactional
    public PaymentTransaction confirmPayment(PaymentRequest request, String idempotencyKey) {
        log.info("Received payment request: upiId={}, amount={}, idempotencyKey={}",
                request.getUpiId(), request.getAmount(), idempotencyKey);

        Optional<PaymentTransaction> existing = repository.findByIdempotencyKey(idempotencyKey);

        // 🔁 RETRY CASE
        if (existing.isPresent()) {
            PaymentTransaction txn = existing.get();
            log.info("Existing transaction found: txnId={}, status={}", txn.getTransactionId(), txn.getStatus());

            // ✅ Already processed
            if (txn.getStatus() == PaymentStatus.SUCCESS || txn.getStatus() == PaymentStatus.FAILED) {
                log.info("Transaction already completed. Returning existing transaction.");
                return txn;
            }

            return txn;
        }

        PaymentTransaction txn = PaymentTransaction.builder()
                .upiId(request.getUpiId())
                .amount(request.getAmount())
                .idempotencyKey(idempotencyKey)
                .transactionId("TXN-" + UUID.randomUUID())
                .status(PaymentStatus.PENDING)  // initial status pending
                .build();

        repository.save(txn);
        log.info("Transaction saved as PENDING: txnId={}", txn.getTransactionId());

        // Simulate async third-party bank payment
        log.info("Calling fake bank service for processing txnId={}", txn.getTransactionId());
        fakeBankService.processPaymentAsync(idempotencyKey, txn.getTransactionId());

        return txn;
    }

    @Transactional
    public void markSuccess(String idempotencyKey, String utrNumber) {
        PaymentTransaction txn = repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalStateException("Transaction not found for idempotencyKey=" + idempotencyKey));

        if ("SUCCESS".equals(txn.getStatus())) {
            log.info("Transaction already marked as SUCCESS: txnId={}", txn.getTransactionId());
            return;
        }

        txn.setUtrNumber(utrNumber);
        txn.setStatus(PaymentStatus.SUCCESS);
        log.info("Transaction marked SUCCESS: txnId={}, utrNumber={}", txn.getTransactionId(), utrNumber);
    }
}
