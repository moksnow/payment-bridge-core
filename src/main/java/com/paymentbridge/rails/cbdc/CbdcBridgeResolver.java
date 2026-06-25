package com.paymentbridge.rails.cbdc;

import com.paymentbridge.common.enums.CbdcNetwork;
import com.paymentbridge.common.enums.CbdcOperationType;
import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.payment.entity.Payment;
import org.springframework.stereotype.Component;

/*
 * CBDC Bridge Resolver — the core bridge logic.
 *
 * Determines which operation type a payment should use
 * and through which CBDC network it should be routed.
 *
 * ─── Operation Matrix ───────────────────────────────────
 * From     │ To       │ Operation │ Network
 * ─────────┼──────────┼───────────┼────────────────────────
 * USD/EUR  │ USDC     │ MINT      │ ECB_SANDBOX
 * USD/EUR  │ USDT     │ MINT      │ FED_SANDBOX
 * USDC     │ USD/EUR  │ REDEEM    │ ECB_SANDBOX
 * USDT     │ USD/EUR  │ REDEEM    │ FED_SANDBOX
 * USDC     │ USDC     │ TRANSFER  │ ECB_SANDBOX (same network)
 * USDT     │ USDT     │ TRANSFER  │ FED_SANDBOX (same network)
 * USDC     │ USDT     │ SWAP      │ BIS_MBRIDGE (cross-network)
 * USDT     │ USDC     │ SWAP      │ BIS_MBRIDGE (cross-network)
 * ────────────────────────────────────────────────────────
 *
 * Aligned with real-world behavior:
 * - MINT/REDEEM: bridge between fiat and digital currency
 * - TRANSFER: transfer within the same network
 * - SWAP: exchange between different networks (e.g. BIS mBridge)
 */

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Component
public class CbdcBridgeResolver {

    /**
     * Determines the operation type based on source and target currencies.
     */
    public CbdcOperationType resolveOperation(Currency from, Currency to) {
        if (from == null || to == null) return CbdcOperationType.TRANSFER;

        boolean fromIsFiat = from.isFiat();
        boolean toIsFiat = to.isFiat();
        boolean fromIsCbdc = !fromIsFiat;
        boolean toIsCbdc = !toIsFiat;

        // Fiat → CBDC: MINT (entering the digital network)
        if (fromIsFiat && toIsCbdc) return CbdcOperationType.MINT;

        // CBDC → Fiat: REDEEM (leaving the digital network)
        if (fromIsCbdc && toIsFiat) return CbdcOperationType.REDEEM;

        // CBDC → Same CBDC: TRANSFER (within one network)
        if (fromIsCbdc && toIsCbdc && from.equals(to)) return CbdcOperationType.TRANSFER;

        // CBDC → Different CBDC: SWAP (cross-network — BIS mBridge)
        if (fromIsCbdc && toIsCbdc && !from.equals(to)) return CbdcOperationType.SWAP;

        return CbdcOperationType.TRANSFER;
    }

    /**
     * Determines the appropriate CBDC network based on the source currency.
     */
    public CbdcNetwork resolveNetwork(Currency from, Currency to) {
        if (from == null) return CbdcNetwork.INTERNAL;

        // Swap between different networks → BIS mBridge
        if (to != null && !from.equals(to)
                && !from.isFiat() && !to.isFiat()) {
            return CbdcNetwork.BIS_MBRIDGE;
        }

        // Determine network based on source currency
        return switch (from) {
            case USDC -> CbdcNetwork.ECB_SANDBOX;
            case USDT -> CbdcNetwork.FED_SANDBOX;
            case USD, EUR -> to != null && !to.isFiat()
                    ? resolveNetwork(to, null)   // MINT: destination network
                    : CbdcNetwork.INTERNAL;
            default -> CbdcNetwork.INTERNAL;
        };
    }

    /**
     * Determines whether this payment is cross-network
     * and requires BIS mBridge.
     */
    public boolean isCrossNetwork(Payment payment) {
        Currency from = payment.getCurrency();
        Currency to = payment.getReceiveCurrency();
        if (from == null || to == null) return false;
        if (from.isFiat() || to.isFiat()) return false;

        // Both are CBDCs but different → cross-network
        return !from.equals(to);
    }
}