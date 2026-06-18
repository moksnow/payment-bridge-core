package com.paymentbridge.wallet.dto;

import com.paymentbridge.common.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Moh Khandan
 * Date: 6/18/2026
 * Time: 9:25 AM
 */
@Data
public class DepositRequest {

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 18, fraction = 10)
    private BigDecimal amount;

    @NotNull
    private Currency currency;
}
