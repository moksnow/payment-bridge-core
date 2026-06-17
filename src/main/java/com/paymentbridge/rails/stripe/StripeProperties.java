package com.paymentbridge.rails.stripe;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Moh Khandan
 * Date: 16/6/2026
 * Time: 19:57 PM
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.rails.stripe")
public class StripeProperties {

    private String apiKey;
    private String webhookSecret;
}
