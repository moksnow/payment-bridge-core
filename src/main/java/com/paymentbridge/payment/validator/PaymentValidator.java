package com.paymentbridge.payment.validator;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.common.enums.PaymentRailType;
import com.paymentbridge.exception.InvalidPaymentException;
import com.paymentbridge.payment.dto.CreatePaymentRequest;
import com.paymentbridge.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/*
 * Business validation logic before sending to Rail.
 * Bean Validation (@NotNull, etc.) is handled in the DTO.
 * Only business rules are checked here.
 */

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");

    // Determines which currencies are supported by each rail.
    private static final Map<PaymentRailType, Set<Currency>> RAIL_CURRENCY_SUPPORT = Map.of(
            PaymentRailType.MOCK, EnumSet.allOf(Currency.class),
            PaymentRailType.STRIPE, EnumSet.of(Currency.USD, Currency.EUR, Currency.GBP),
            PaymentRailType.CBDC_SANDBOX, EnumSet.of(Currency.USDC, Currency.USDT, Currency.USD, Currency.EUR, Currency.GBP)
    );

    private final WalletService walletService;

    public void validate(CreatePaymentRequest req, String userId) {

        // Amount Limits
        if (req.getAmount().compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidPaymentException("Amount must be at least " + MIN_AMOUNT);
        }
        if (req.getAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidPaymentException("Amount exceeds maximum: " + MAX_AMOUNT);
        }

        String senderAccountCode = walletService.getLedgerAccountCode(userId, req.getCurrency());
        if (senderAccountCode.equals(req.getReceiverWalletAccountCode())) {
            throw new InvalidPaymentException("Cannot send payment to your own wallet");
        }

        // rail/currency compatibility
        Set<Currency> supported = RAIL_CURRENCY_SUPPORT.get(req.getRailType());
        if (supported != null && !supported.contains(req.getCurrency())) {
            throw new InvalidPaymentException(
                    "Rail [" + req.getRailType() + "] does not support currency [" + req.getCurrency() + "]");
        }
    }
}
