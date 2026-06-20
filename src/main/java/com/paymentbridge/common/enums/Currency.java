package com.paymentbridge.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {

    USD("US Dollar", 2),
    EUR("Euro", 2),
    GBP("British Pound", 2),
    AED("UAE Dirham", 2),
    TRY("Turkish Lira", 2),
    USDT("Tether USD", 6),
    USDC("USD Coin", 6);

    private final String displayName;
    private final int decimalPlaces;
}
