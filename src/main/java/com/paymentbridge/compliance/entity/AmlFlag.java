package com.paymentbridge.compliance.entity;

import com.paymentbridge.common.enums.AmlStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Moh Khandan
 * Date: 06/24/2026
 * Time: 10:01 PM
 */
@Entity
@Table(name = "aml_flags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlFlag {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AmlStatus status;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "amount", precision = 30, scale = 10)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }
}
