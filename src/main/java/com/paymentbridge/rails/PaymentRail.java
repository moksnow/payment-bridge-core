package com.paymentbridge.rails;

import com.paymentbridge.common.enums.PaymentRailType;
import com.paymentbridge.payment.entity.Payment;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
public interface PaymentRail {

    PaymentRailType railType();

    boolean supports(Payment payment);

    RailResult process(Payment payment);
}
