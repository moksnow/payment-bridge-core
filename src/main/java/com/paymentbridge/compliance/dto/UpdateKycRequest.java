package com.paymentbridge.compliance.dto;

import com.paymentbridge.common.enums.KycLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Moh Khandan
 * Date: 06/24/2026
 * Time: 10:01 PM
 */
@Data
public class UpdateKycRequest {

    @NotNull(message = "KYC level is required")
    private KycLevel kycLevel;

    private String notes;
}
