package com.paymentbridge.fx.provider;

import com.paymentbridge.common.enums.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Moh Khandan
 * Date: 06/19/2026
 * Time: 5:07 PM
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.fx.provider", havingValue = "mock", matchIfMissing = true)
public class MockFxRateProvider implements FxRateProvider {

    private static final Map<Currency, BigDecimal> RATES_FROM_USD = Map.of(
            Currency.USD, new BigDecimal("1.000000"),
            Currency.EUR, new BigDecimal("0.910000"),
            Currency.GBP, new BigDecimal("0.790000"),
            Currency.AED, new BigDecimal("3.670000"),
            Currency.TRY, new BigDecimal("32.500000"),
            Currency.USDT, new BigDecimal("1.000000"),
            Currency.USDC, new BigDecimal("1.000000")
    );

    @Override
    public BigDecimal getRate(Currency from, Currency to) {
        if (from.equals(to)) return BigDecimal.ONE;

        BigDecimal fromRate = RATES_FROM_USD.getOrDefault(from, BigDecimal.ONE);
        BigDecimal toRate = RATES_FROM_USD.getOrDefault(to, BigDecimal.ONE);

        // cross-rate: to / from
        BigDecimal rate = toRate.divide(fromRate, 10, java.math.RoundingMode.HALF_EVEN);
        log.debug("MockFX rate {}/{} = {}", from, to, rate);
        return rate;
    }

    @Override
    public String providerName() {
        return "MOCK";
    }
}
