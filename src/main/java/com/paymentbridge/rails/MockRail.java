package com.paymentbridge.rails;

import com.paymentbridge.common.enums.PaymentRailType;
import com.paymentbridge.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * شبیه‌ساز rail واقعی برای MVP.
 * در production جای این را PSP یا CBDC adapter می‌گیرد.
 */
/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Slf4j
@Component
public class MockRail implements PaymentRail {

    @Override
    public PaymentRailType railType() {
        return PaymentRailType.MOCK;
    }

    @Override
    public boolean supports(Payment payment) {
        return payment.getRailType() == PaymentRailType.MOCK;
    }

    @Override
    public RailResult process(Payment payment) {
        log.info("MockRail processing payment [{}] amount [{} {}]",
                payment.getId(), payment.getAmount(), payment.getCurrency());

        simulateNetworkDelay();

        if (shouldFail(payment)) {
            log.warn("MockRail simulated failure for payment [{}]", payment.getId());
            return RailResult.failure("MOCK_FAILURE", "Simulated rail failure");
        }

        String externalRef = "mock-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("MockRail success for payment [{}] ref [{}]", payment.getId(), externalRef);
        return RailResult.success(externalRef);
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean shouldFail(Payment payment) {
        return payment.getDescription() != null
                && payment.getDescription().toLowerCase().contains("fail");
    }
}
