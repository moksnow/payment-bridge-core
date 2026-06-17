package com.paymentbridge.payment.dto;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.common.enums.PaymentRailType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Data
public class CreatePaymentRequest {

    // senderAccount حذف شد — از JWT token خونده می‌شه

    @NotBlank(message = "Receiver account is required")
    private String receiverAccount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 18, fraction = 10)
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Rail type is required")
    private PaymentRailType railType;

    @Size(max = 500)
    private String description;
}
