package com.paymentbridge.ledger;

import com.paymentbridge.ledger.service.LedgerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@SpringBootTest
class LedgerServiceTest {

    // test: two entries created per payment (debit + credit)
    // test: entries are immutable (no update)
    // implementation will be added next
}
