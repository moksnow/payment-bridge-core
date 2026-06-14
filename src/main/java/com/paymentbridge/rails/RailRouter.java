package com.paymentbridge.rails;

import com.paymentbridge.common.enums.PaymentRailType;
import com.paymentbridge.exception.RailException;
import com.paymentbridge.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Slf4j
@Component
public class RailRouter {

    private final Map<PaymentRailType, PaymentRail> railMap;

    public RailRouter(List<PaymentRail> rails) {
        this.railMap = rails.stream()
                .collect(Collectors.toMap(PaymentRail::railType, Function.identity()));
        log.info("RailRouter initialized with rails: {}", railMap.keySet());
    }

    public PaymentRail route(Payment payment) {
        PaymentRailType requestedType = payment.getRailType();

        PaymentRail rail = railMap.get(requestedType);
        if (rail != null && rail.supports(payment)) {
            log.debug("Routing payment [{}] to rail [{}]", payment.getId(), requestedType);
            return rail;
        }

        PaymentRail mockRail = railMap.get(PaymentRailType.MOCK);
        if (mockRail != null) {
            log.warn("Rail [{}] not available, falling back to MOCK for payment [{}]",
                    requestedType, payment.getId());
            return mockRail;
        }

        throw new RailException(requestedType,
                "No rail available for type [" + requestedType + "]");
    }
}
