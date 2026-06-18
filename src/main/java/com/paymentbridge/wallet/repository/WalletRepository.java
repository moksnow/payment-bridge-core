package com.paymentbridge.wallet.repository;

import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Moh Khandan
 * Date: 6/18/2026
 * Time: 9:25 AM
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {

    List<Wallet> findByUserId(String userId);

    Optional<Wallet> findByUserIdAndCurrency(String userId, Currency currency);

    Optional<Wallet> findByLedgerAccountCode(String ledgerAccountCode);
}
