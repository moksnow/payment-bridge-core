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

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "sender_account", nullable = false, length = 100)
    private String senderAccount;

    @Column(name = "receiver_account", nullable = false, length = 100)
    private String receiverAccount;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "rail_type", nullable = false, length = 30)
    private PaymentRailType railType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "user_id")
    private String userId;

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
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // ── business helpers ──

    public void markProcessing() {
        this.status = PaymentStatus.PROCESSING;
    }

    public void markCompleted(String externalRef) {
        this.status      = PaymentStatus.COMPLETED;
        this.externalRef = externalRef;
    }

    public void markFailed(String reason) {
        this.status        = PaymentStatus.FAILED;
        this.failureReason = reason;
    }
}
