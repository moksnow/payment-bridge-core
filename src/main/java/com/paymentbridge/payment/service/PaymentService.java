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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
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
        log.info("Initiating payment idempotencyKey=[{}]", idempotencyKey);

        // business validation
        paymentValidator.validate(req);

        // Idempotency check — also enforced by a DB unique constraint,
        // but validated here first.
        if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new DuplicateRequestException(idempotencyKey);
        }

        // Create and Save Payment with PENDING Status
        Payment payment = Payment.builder()
                .idempotencyKey(idempotencyKey)
                .senderAccount(req.getSenderAccount())
                .receiverAccount(req.getReceiverAccount())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .railType(req.getRailType())
                .description(req.getDescription())
                .build();
        paymentRepository.save(payment);
        log.debug("Payment saved with status PENDING id=[{}]", payment.getId());

        // Forward Request to the Rail
        payment.markProcessing();
        paymentRepository.save(payment);

        RailResult result = railRouter.route(payment).process(payment);

        if (result.isSuccess()) {
            payment.markCompleted(result.getExternalRef());
            paymentRepository.save(payment);
            ledgerService.postPayment(payment);
            log.info("Payment [{}] COMPLETED externalRef=[{}]", payment.getId(), result.getExternalRef());
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
        return paymentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
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
