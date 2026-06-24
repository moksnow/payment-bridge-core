package com.paymentbridge.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Moh Khandan
 * Date: 06/24/2026
 * Time: 10:01 PM
 */
@Getter
@RequiredArgsConstructor
public enum KycLevel {

    UNVERIFIED(
            new BigDecimal("100.00"),
            new BigDecimal("500.00"),
            "No identity verification"
    ),
    BASIC(
            new BigDecimal("5000.00"),
            new BigDecimal("20000.00"),
            "Basic identity verified"
    ),
    FULL(
            new BigDecimal("1000000.00"),
            new BigDecimal("1000000.00"),
            "Full identity verified"
    );

    private final BigDecimal maxTransactionAmount;
    private final BigDecimal dailyLimit;
    private final String     description;
}
