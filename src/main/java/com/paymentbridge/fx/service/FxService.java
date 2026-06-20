package com.paymentbridge.fx.service;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.fx.dto.FxConversionResult;
import com.paymentbridge.fx.dto.FxRateResponse;
import com.paymentbridge.fx.provider.FxRateProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * @author Moh Khandan
 * Date: 06/19/2026
 * Time: 5:07 PM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FxService {

    private final FxRateProvider fxRateProvider;

    public FxRateResponse getRate(Currency from, Currency to) {
        BigDecimal rate = fxRateProvider.getRate(from, to);
        return FxRateResponse.builder()
                .from(from)
                .to(to)
                .rate(rate)
                .fetchedAt(Instant.now())
                .mock(fxRateProvider.providerName().equals("MOCK"))
                .build();
    }

    public FxConversionResult convert(Currency from, Currency to, BigDecimal amount) {
        if (from.equals(to)) {
            return FxConversionResult.builder()
                    .fromAmount(amount)
                    .fromCurrency(from)
                    .toAmount(amount)
                    .toCurrency(to)
                    .rate(BigDecimal.ONE)
                    .build();
        }

        BigDecimal rate = fxRateProvider.getRate(from, to);
        BigDecimal converted = amount.multiply(rate)
                .setScale(to.getDecimalPlaces(), RoundingMode.HALF_EVEN);

        log.info("FX convert {} {} → {} {} (rate={})",
                amount, from, converted, to, rate);

        return FxConversionResult.builder()
                .fromAmount(amount)
                .fromCurrency(from)
                .toAmount(converted)
                .toCurrency(to)
                .rate(rate)
                .build();
    }

    public boolean needsConversion(Currency from, Currency to) {
        return to != null && !from.equals(to);
    }
}
