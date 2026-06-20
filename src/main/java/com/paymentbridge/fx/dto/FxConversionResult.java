package com.paymentbridge.fx.dto;

import com.paymentbridge.common.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Moh Khandan
 * Date: 06/19/2026
 * Time: 5:07 PM
 */
@Data
@Builder
public class FxConversionResult {
    private BigDecimal fromAmount;
    private Currency fromCurrency;
    private BigDecimal toAmount;
    private Currency toCurrency;
    private BigDecimal rate;
}
