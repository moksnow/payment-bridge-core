package com.paymentbridge.wallet.dto;

import com.paymentbridge.common.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author Moh Khandan
 * Date: 6/18/2026
 * Time: 9:25 AM
 */
@Data
@Builder
public class WalletResponse {
    private String id;
    private String userId;
    private Currency currency;
    private BigDecimal balance;
    private String ledgerAccountCode;
    private Instant createdAt;
}
