package com.paymentbridge.ledger.dto;

import com.paymentbridge.common.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Data
@Builder
public class LedgerEntryResponse {

    private String     id;
    private String     paymentId;
    private String     accountCode;
    private String     entryType;
    private BigDecimal amount;
    private Currency   currency;
    private String     description;
    private Instant    createdAt;
}
