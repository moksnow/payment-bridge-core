package com.paymentbridge.fx.provider;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * @author Moh Khandan
 * Date: 06/19/2026
 * Time: 5:07 PM
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.fx.provider", havingValue = "exchangeratesapi")
public class ExchangeRatesApiFxProvider implements FxRateProvider {

    private final RestTemplate restTemplate;
    @Value("${app.fx.api-key}")
    private String apiKey;
    @Value("${app.fx.base-url:http://api.exchangeratesapi.io/v1}")
    private String baseUrl;

    @Override
    @SuppressWarnings("unchecked")
    public BigDecimal getRate(Currency from, Currency to) {
        if (from.equals(to)) return BigDecimal.ONE;

        try {
            String url = baseUrl + "/latest?access_key=" + apiKey
                    + "&base=EUR&symbols="
                    + from.name() + "," + to.name();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
                throw new BusinessException("FX_ERROR",
                        "FX API returned unsuccessful response",
                        HttpStatus.SERVICE_UNAVAILABLE);
            }

            Map<String, Number> rates = (Map<String, Number>) response.get("rates");

            // هر دو نرخ نسبت به EUR هستند
            // rate(EUR→from) و rate(EUR→to) →  cross = to / from
            double fromRate = rates.get(from.name()).doubleValue();
            double toRate = rates.get(to.name()).doubleValue();

            BigDecimal rate = BigDecimal.valueOf(toRate / fromRate)
                    .setScale(10, RoundingMode.HALF_EVEN);

            log.info("ExchangeRatesAPI rate {}/{} = {} (via EUR)", from, to, rate);
            return rate;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("ExchangeRatesAPI call failed: {}", e.getMessage());
            throw new BusinessException("FX_UNAVAILABLE",
                    "FX rate service unavailable: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public String providerName() {
        return "EXCHANGERATESAPI";
    }
}
