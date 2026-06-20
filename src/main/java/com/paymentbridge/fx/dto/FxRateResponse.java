package com.paymentbridge.fx.dto;

import com.paymentbridge.common.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author Moh Khandan
 * Date: 06/19/2026
 * Time: 5:07 PM
 */
@Data
@Builder
public class FxRateResponse {
    private Currency from;
    private Currency to;
    private BigDecimal rate;
    private Instant fetchedAt;
    private boolean mock;
}
