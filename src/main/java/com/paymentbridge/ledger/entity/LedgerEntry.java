package com.paymentbridge.ledger.entity;

import com.paymentbridge.common.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Entity
@Table(name = "ledger_entries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "payment_id", nullable = false, updatable = false)
    private String paymentId;

    @Column(name = "account_code", nullable = false, updatable = false, length = 100)
    private String accountCode;

    @Column(name = "entry_type", nullable = false, updatable = false, length = 10)
    private String entryType; // DEBIT | CREDIT

    @Column(nullable = false, updatable = false, precision = 30, scale = 10)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private Currency currency;

    @Column(updatable = false, length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        this.createdAt = Instant.now();
    }

    public static LedgerEntry debit(String paymentId, String accountCode,
                                    BigDecimal amount, Currency currency, String description) {
        return LedgerEntry.builder()
                .paymentId(paymentId)
                .accountCode(accountCode)
                .entryType("DEBIT")
                .amount(amount)
                .currency(currency)
                .description(description)
                .build();
    }

    public static LedgerEntry credit(String paymentId, String accountCode,
                                     BigDecimal amount, Currency currency, String description) {
        return LedgerEntry.builder()
                .paymentId(paymentId)
                .accountCode(accountCode)
                .entryType("CREDIT")
                .amount(amount)
                .currency(currency)
                .description(description)
                .build();
    }
}
