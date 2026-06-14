package com.paymentbridge.ledger.service;

import com.paymentbridge.common.constants.AppConstants;
import com.paymentbridge.ledger.dto.LedgerEntryResponse;
import com.paymentbridge.ledger.entity.LedgerEntry;
import com.paymentbridge.ledger.repository.LedgerEntryRepository;
import com.paymentbridge.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * تنها سرویسی که مجاز به نوشتن ledger entries است.
 * هر پرداخت دقیقاً دو entry ایجاد می‌کند: DEBIT + CREDIT
 */

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public void postPayment(Payment payment) {
        log.info("Posting ledger entries for payment [{}]", payment.getId());

        LedgerEntry debit = LedgerEntry.debit(
                payment.getId(),
                payment.getSenderAccount(),
                payment.getAmount(),
                payment.getCurrency(),
                "Payment sent to: " + payment.getReceiverAccount()
        );

        LedgerEntry credit = LedgerEntry.credit(
                payment.getId(),
                payment.getReceiverAccount(),
                payment.getAmount(),
                payment.getCurrency(),
                "Payment received from: " + payment.getSenderAccount()
        );

        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        log.info("Ledger entries posted for payment [{}] — DEBIT [{}] CREDIT [{}]",
                payment.getId(), debit.getId(), credit.getId());
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getByPaymentId(String paymentId) {
        return ledgerEntryRepository
                .findByPaymentIdOrderByCreatedAtAsc(paymentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getByAccountCode(String accountCode) {
        return ledgerEntryRepository
                .findByAccountCode(accountCode)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LedgerEntryResponse toResponse(LedgerEntry entry) {
        return LedgerEntryResponse.builder()
                .id(entry.getId())
                .paymentId(entry.getPaymentId())
                .accountCode(entry.getAccountCode())
                .entryType(entry.getEntryType())
                .amount(entry.getAmount())
                .currency(entry.getCurrency())
                .description(entry.getDescription())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
