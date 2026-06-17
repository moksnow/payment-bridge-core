package com.paymentbridge.ledger.controller;

import com.paymentbridge.common.constants.AppConstants;
import com.paymentbridge.ledger.dto.LedgerEntryResponse;
import com.paymentbridge.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@RestController
@RequestMapping(AppConstants.LEDGER_PATH)
@RequiredArgsConstructor
@Tag(name = "Ledger", description = "Double-entry ledger query API")
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/payments/{paymentId}")
    @Operation(summary = "Get ledger entries for a payment")
    public ResponseEntity<List<LedgerEntryResponse>> getByPaymentId(
            @PathVariable String paymentId) {
        return ResponseEntity.ok(ledgerService.getByPaymentId(paymentId));
    }

    @GetMapping("/accounts/{accountCode}")
    @Operation(summary = "Get ledger entries for an account")
    public ResponseEntity<List<LedgerEntryResponse>> getByAccountCode(
            @PathVariable String accountCode) {
        return ResponseEntity.ok(ledgerService.getByAccountCode(accountCode));
    }
}
