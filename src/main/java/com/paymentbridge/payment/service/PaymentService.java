package com.paymentbridge.payment.service;

import com.paymentbridge.exception.DuplicateRequestException;
import com.paymentbridge.exception.NotFoundException;
import com.paymentbridge.fx.dto.FxConversionResult;
import com.paymentbridge.fx.service.FxService;
import com.paymentbridge.ledger.service.LedgerService;
import com.paymentbridge.payment.dto.CreatePaymentRequest;
import com.paymentbridge.payment.dto.PaymentResponse;
import com.paymentbridge.payment.entity.Payment;
import com.paymentbridge.payment.repository.PaymentRepository;
import com.paymentbridge.payment.validator.PaymentValidator;
import com.paymentbridge.rails.RailResult;
import com.paymentbridge.rails.RailRouter;
import com.paymentbridge.wallet.service.WalletService;
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
    private final LedgerService ledgerService;
    private final RailRouter railRouter;
    private final PaymentValidator paymentValidator;
    private final WalletService walletService;
    private final FxService fxService;

    @Transactional
    public PaymentResponse initiate(CreatePaymentRequest req, String idempotencyKey) {

        String userId = (String) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        log.info("Initiating payment userId=[{}] idempotencyKey=[{}]", userId, idempotencyKey);

        // ── validation ──
        paymentValidator.validate(req, userId);

        // ── idempotency check ──
        if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new DuplicateRequestException(idempotencyKey);
        }

        // ── balance check ──
        walletService.assertSufficientBalance(userId, req.getCurrency(), req.getAmount());

        // ── FX conversion (اگه receiveCurrency متفاوت بود) ──
        FxConversionResult fx = null;
        if (fxService.needsConversion(req.getCurrency(), req.getReceiveCurrency())) {
            fx = fxService.convert(req.getCurrency(), req.getReceiveCurrency(), req.getAmount());
            log.info("FX applied: {} {} → {} {}",
                    fx.getFromAmount(), fx.getFromCurrency(),
                    fx.getToAmount(), fx.getToCurrency());
        }

        // ── ledgerAccountCode های sender و receiver ──
        String senderAccountCode = walletService.getLedgerAccountCode(userId, req.getCurrency());
        String receiverAccountCode = req.getReceiverWalletAccountCode();

        // ── ساخت payment ──
        Payment payment = Payment.builder()
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .senderWalletAccount(senderAccountCode)
                .receiverWalletAccount(receiverAccountCode)
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .receiveAmount(fx != null ? fx.getToAmount() : req.getAmount())
                .receiveCurrency(fx != null ? fx.getToCurrency() : req.getCurrency())
                .fxRate(fx != null ? fx.getRate() : null)
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
                .senderWalletAccount(p.getSenderWalletAccount())
                .receiverWalletAccount(p.getReceiverWalletAccount())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .receiveAmount(p.getReceiveAmount())
                .receiveCurrency(p.getReceiveCurrency())
                .fxRate(p.getFxRate())
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
