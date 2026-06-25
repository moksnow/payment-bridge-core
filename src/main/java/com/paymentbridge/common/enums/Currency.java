package com.paymentbridge.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {

    // ── Fiat ──
    USD("US Dollar", 2, true),
    EUR("Euro", 2, true),
    GBP("British Pound", 2, true),
    AED("UAE Dirham", 2, true),
    TRY("Turkish Lira", 2, true),

    // ── Stablecoin / CBDC ──
    USDT("Tether USD", 6, false),
    USDC("USD Coin", 6, false);

    private final String displayName;
    private final int decimalPlaces;
    private final boolean fiat;

    public boolean isCbdc() {
        return !fiat;
    }
}
