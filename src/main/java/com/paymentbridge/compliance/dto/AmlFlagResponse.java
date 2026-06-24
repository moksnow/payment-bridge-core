package com.paymentbridge.compliance.dto;

import com.paymentbridge.common.enums.AmlStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author Moh Khandan
 * Date: 06/24/2026
 * Time: 10:01 PM
 */
@Data
@Builder
public class AmlFlagResponse {
    private String id;
    private String paymentId;
    private String userId;
    private AmlStatus status;
    private String reason;
    private BigDecimal amount;
    private Instant createdAt;
}
