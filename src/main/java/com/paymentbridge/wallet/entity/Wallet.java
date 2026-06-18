package com.paymentbridge.wallet.entity;

import com.paymentbridge.common.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Moh Khandan
 * Date: 6/18/2026
 * Time: 9:25 AM
 */
@Entity
@Table(
        name = "wallets",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_wallet_user_currency",
                columnNames = {"user_id", "currency"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(name = "ledger_account_code", nullable = false, unique = true)
    private String ledgerAccountCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        this.createdAt = Instant.now();
        if (this.ledgerAccountCode == null) {
            this.ledgerAccountCode = "WALLET-" + this.userId + "-" + this.currency.name();
        }
    }
}
