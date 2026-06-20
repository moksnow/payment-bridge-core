package com.paymentbridge.fx.provider;

import com.paymentbridge.common.enums.Currency;

import java.math.BigDecimal;

/**
 * @author Moh Khandan
 * Date: 06/19/2026
 * Time: 5:07 PM
 */
public interface FxRateProvider {

    BigDecimal getRate(Currency from, Currency to);

    String providerName();
}
