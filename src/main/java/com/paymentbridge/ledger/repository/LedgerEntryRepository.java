package com.paymentbridge.ledger.repository;

import com.paymentbridge.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, String> {

    List<LedgerEntry> findByPaymentIdOrderByCreatedAtAsc(String paymentId);

    List<LedgerEntry> findByAccountCode(String accountCode);
}
