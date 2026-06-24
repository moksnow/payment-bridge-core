package com.paymentbridge.compliance.dto;

import com.paymentbridge.common.enums.KycLevel;
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
public class KycProfileResponse {
    private String id;
    private String userId;
    private KycLevel kycLevel;
    private BigDecimal maxTransactionAmount;
    private BigDecimal dailyLimit;
    private String notes;
    private Instant updatedAt;
}
