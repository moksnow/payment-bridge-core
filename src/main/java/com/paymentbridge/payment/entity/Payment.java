package com.paymentbridge.payment.entity;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.common.enums.PaymentRailType;
import com.paymentbridge.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    /**
     * ledgerAccountCode sender wallet — for example, "WALLET-{userId}-USD"
     * It comes from the sender's wallet, not directly from the request
     */
    @Column(name = "sender_wallet_account", nullable = false, length = 100)
    private String senderWalletAccount;

    /**
     * receiver's ledgerAccountCode wallet
     */
    @Column(name = "receiver_wallet_account", nullable = false, length = 100)
    private String receiverWalletAccount;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(name = "receive_amount", precision = 30, scale = 10)
    private BigDecimal receiveAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "receive_currency", length = 10)
    private Currency receiveCurrency;

    @Column(name = "fx_rate", precision = 20, scale = 10)
    private BigDecimal fxRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "rail_type", nullable = false, length = 30)
    private PaymentRailType railType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 500)
    private String description;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "external_ref", length = 255)
    private String externalRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = PaymentStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean hasFxConversion() {
        return fxRate != null && receiveCurrency != null && !currency.equals(receiveCurrency);
    }

    public void markProcessing() {
        this.status = PaymentStatus.PROCESSING;
    }

    public void markCompleted(String externalRef) {
        this.status = PaymentStatus.COMPLETED;
        this.externalRef = externalRef;
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }
}
