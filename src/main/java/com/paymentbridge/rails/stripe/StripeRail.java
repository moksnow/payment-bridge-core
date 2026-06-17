package com.paymentbridge.rails.stripe;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.common.enums.PaymentRailType;
import com.paymentbridge.exception.RailException;
import com.paymentbridge.payment.entity.Payment;
import com.paymentbridge.rails.PaymentRail;
import com.paymentbridge.rails.RailResult;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Moh Khandan
 * Date: 16/6/2026
 * Time: 19:57 PM
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StripeRail implements PaymentRail {

    private static final Set<Currency> SUPPORTED_CURRENCIES = Set.of(
            Currency.USD,
            Currency.EUR,
            Currency.GBP
    );

    private final StripeProperties stripeProperties;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.getApiKey();
        log.info("StripeRail initialized (sandbox mode)");
    }

    @Override
    public PaymentRailType railType() {
        return PaymentRailType.STRIPE;
    }

    @Override
    public boolean supports(Payment payment) {
        return payment.getRailType() == PaymentRailType.STRIPE
                && SUPPORTED_CURRENCIES.contains(payment.getCurrency());
    }

    @Override
    public RailResult process(Payment payment) {
        log.info("StripeRail processing payment [{}] amount [{} {}]",
                payment.getId(), payment.getAmount(), payment.getCurrency());

        try {
            long amountInSmallestUnit = payment.getAmount()
                    .movePointRight(2)
                    .longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInSmallestUnit)
                    .setCurrency(payment.getCurrency().name().toLowerCase())
                    .setDescription(payment.getDescription())
                    .putMetadata("payment_id", payment.getId())
                    .putMetadata("sender_account", payment.getSenderAccount())
                    .putMetadata("receiver_account", payment.getReceiverAccount())
                    .putMetadata("idempotency_key", payment.getIdempotencyKey())
                    .setConfirm(false)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            log.info("StripeRail success paymentIntentId=[{}] status=[{}]",
                    intent.getId(), intent.getStatus());

            return RailResult.success(intent.getId());

        } catch (StripeException e) {
            log.error("StripeRail failed for payment [{}]: code=[{}] message=[{}]",
                    payment.getId(), e.getCode(), e.getMessage());
            throw new RailException(PaymentRailType.STRIPE,
                    "Stripe error [" + e.getCode() + "]: " + e.getMessage());
        }
    }
}
