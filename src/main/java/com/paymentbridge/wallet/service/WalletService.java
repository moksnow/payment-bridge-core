package com.paymentbridge.wallet.service;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.exception.BusinessException;
import com.paymentbridge.exception.InsufficientFundsException;
import com.paymentbridge.exception.NotFoundException;
import com.paymentbridge.ledger.entity.LedgerEntry;
import com.paymentbridge.ledger.repository.LedgerEntryRepository;
import com.paymentbridge.wallet.dto.DepositRequest;
import com.paymentbridge.wallet.dto.WalletResponse;
import com.paymentbridge.wallet.entity.Wallet;
import com.paymentbridge.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Moh Khandan
 * Date: 6/18/2026
 * Time: 9:25 AM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public WalletResponse create(String userId, Currency currency) {
        if (walletRepository.findByUserIdAndCurrency(userId, currency).isPresent()) {
            throw new BusinessException("WALLET_EXISTS",
                    "Wallet already exists for currency: " + currency,
                    HttpStatus.CONFLICT);
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .currency(currency)
                .build();

        wallet = walletRepository.save(wallet);
        log.info("Wallet created userId=[{}] currency=[{}] accountCode=[{}]",
                userId, currency, wallet.getLedgerAccountCode());

        return toResponse(wallet, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getByUser(String userId) {
        return walletRepository.findByUserId(userId)
                .stream()
                .map(w -> toResponse(w, calculateBalance(w.getLedgerAccountCode())))
                .toList();
    }

    @Transactional
    public WalletResponse deposit(String userId, DepositRequest req) {
        Wallet wallet = walletRepository
                .findByUserIdAndCurrency(userId, req.getCurrency())
                .orElseThrow(() -> new NotFoundException("Wallet",
                        userId + "-" + req.getCurrency()));

        LedgerEntry depositEntry = LedgerEntry.deposit(
                wallet.getLedgerAccountCode(),
                req.getAmount(),
                req.getCurrency(),
                "Sandbox deposit"
        );
        ledgerEntryRepository.save(depositEntry);

        BigDecimal newBalance = calculateBalance(wallet.getLedgerAccountCode());
        log.info("Deposit userId=[{}] amount=[{}] newBalance=[{}]",
                userId, req.getAmount(), newBalance);

        return toResponse(wallet, newBalance);
    }

    @Transactional(readOnly = true)
    public void assertSufficientBalance(String userId, Currency currency, BigDecimal amount) {
        Wallet wallet = walletRepository
                .findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new NotFoundException("Wallet", userId + "-" + currency));

        BigDecimal balance = calculateBalance(wallet.getLedgerAccountCode());
        if (balance.compareTo(amount) < 0) {
            log.warn("Insufficient funds userId=[{}] balance=[{}] required=[{}]",
                    userId, balance, amount);
            throw new InsufficientFundsException(wallet.getLedgerAccountCode());
        }
    }

    @Transactional(readOnly = true)
    public String getLedgerAccountCode(String userId, Currency currency) {
        return walletRepository
                .findByUserIdAndCurrency(userId, currency)
                .map(Wallet::getLedgerAccountCode)
                .orElseThrow(() -> new NotFoundException("Wallet", userId + "-" + currency));
    }

    public BigDecimal calculateBalance(String ledgerAccountCode) {
        return ledgerEntryRepository
                .findByAccountCode(ledgerAccountCode)
                .stream()
                .map(entry -> entry.getEntryType().equals("CREDIT")
                        ? entry.getAmount()
                        : entry.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private WalletResponse toResponse(Wallet wallet, BigDecimal balance) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .currency(wallet.getCurrency())
                .balance(balance)
                .ledgerAccountCode(wallet.getLedgerAccountCode())
                .createdAt(wallet.getCreatedAt())
                .build();
    }
}
