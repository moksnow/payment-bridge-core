package com.paymentbridge.wallet.controller;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.wallet.dto.DepositRequest;
import com.paymentbridge.wallet.dto.WalletResponse;
import com.paymentbridge.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Moh Khandan
 * Date: 6/18/2026
 * Time: 9:25 AM
 */
@RestController
@RequestMapping("/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Wallet management")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @Operation(summary = "Create wallet for a currency")
    public ResponseEntity<WalletResponse> create(@RequestParam Currency currency) {
        String userId = currentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.create(userId, currency));
    }

    @GetMapping
    @Operation(summary = "Get my wallets with balances")
    public ResponseEntity<List<WalletResponse>> getMyWallets() {
        return ResponseEntity.ok(walletService.getByUser(currentUserId()));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds (sandbox only)")
    public ResponseEntity<WalletResponse> deposit(@Valid @RequestBody DepositRequest req) {
        return ResponseEntity.ok(walletService.deposit(currentUserId(), req));
    }

    private String currentUserId() {
        return (String) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }
}
