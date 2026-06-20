package com.paymentbridge.payment.dto;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.common.enums.PaymentRailType;
import com.paymentbridge.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Data
@Builder
public class PaymentResponse {

    private String id;
    private String userId;
    private String idempotencyKey;
    private String senderWalletAccount;
    private String receiverWalletAccount;
    private BigDecimal amount;
    private Currency currency;
    private BigDecimal receiveAmount;
    private Currency receiveCurrency;
    private BigDecimal fxRate;
    private PaymentRailType railType;
    private PaymentStatus status;
    private String description;
    private String failureReason;
    private String externalRef;
    private Instant createdAt;
    private Instant updatedAt;
}
