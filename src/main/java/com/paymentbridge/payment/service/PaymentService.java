package com.paymentbridge.payment.service;

import com.paymentbridge.exception.DuplicateRequestException;
import com.paymentbridge.exception.NotFoundException;
import com.paymentbridge.ledger.service.LedgerService;
import com.paymentbridge.payment.dto.CreatePaymentRequest;
import com.paymentbridge.payment.dto.PaymentResponse;
import com.paymentbridge.payment.entity.Payment;
import com.paymentbridge.payment.repository.PaymentRepository;
import com.paymentbridge.payment.validator.PaymentValidator;
import com.paymentbridge.rails.RailResult;
import com.paymentbridge.rails.RailRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LedgerService     ledgerService;
    private final RailRouter        railRouter;
    private final PaymentValidator  paymentValidator;

    @Transactional
    public PaymentResponse initiate(CreatePaymentRequest req, String idempotencyKey) {

        // ── userId از JWT token ──
        String userId = (String) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        log.info("Initiating payment userId=[{}] idempotencyKey=[{}]", userId, idempotencyKey);

        // ── business validation ──
        paymentValidator.validate(req, userId);

        // ── idempotency check ──
        if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new DuplicateRequestException(idempotencyKey);
        }

        // Create and Save Payment with PENDING Status
        Payment payment = Payment.builder()
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .senderAccount(userId)
                .receiverAccount(req.getReceiverAccount())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .railType(req.getRailType())
                .description(req.getDescription())
                .build();
        paymentRepository.save(payment);

        // Forward Request to the Rail
        payment.markProcessing();
        paymentRepository.save(payment);

        RailResult result = railRouter.route(payment).process(payment);

        if (result.isSuccess()) {
            payment.markCompleted(result.getExternalRef());
            paymentRepository.save(payment);
            ledgerService.postPayment(payment);
            log.info("Payment [{}] COMPLETED", payment.getId());
        } else {
            payment.markFailed(result.getFailureCode() + ": " + result.getFailureMessage());
            paymentRepository.save(payment);
            log.warn("Payment [{}] FAILED reason=[{}]", payment.getId(), payment.getFailureReason());
        }

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(String id) {
        return paymentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Payment", id));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        String userId = (String) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .idempotencyKey(p.getIdempotencyKey())
                .senderAccount(p.getSenderAccount())
                .receiverAccount(p.getReceiverAccount())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .railType(p.getRailType())
                .status(p.getStatus())
                .description(p.getDescription())
                .failureReason(p.getFailureReason())
                .externalRef(p.getExternalRef())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
