package com.paymentbridge.exception;

import com.paymentbridge.common.enums.PaymentRailType;
import org.springframework.http.HttpStatus;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
public class RailException extends BusinessException {

    public RailException(PaymentRailType rail, String message) {
        super("RAIL_ERROR",
              "Rail [" + rail.name() + "] error: " + message,
              HttpStatus.BAD_GATEWAY);
    }
}
