package com.paymentbridge.rails.cbdc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.rails.cbdc")
public class CbdcProperties {

    /**
     * Indicates whether the CBDC rail is enabled.
     */
    private boolean enabled = true;

    /**
     * Sandbox server URL — defaults to the local project instance.
     */
    private String sandboxUrl = "http://localhost:8080/api";

    /**
     * CBDC network identifier.
     */
    private String networkId = "sandbox-network-001";
}
