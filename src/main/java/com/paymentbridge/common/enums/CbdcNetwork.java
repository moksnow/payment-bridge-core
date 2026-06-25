package com.paymentbridge.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
  Supported CBDC/Digital networks in this sandbox.

  Important note about CBDC reality:
  ─────────────────────────────
  No public production-ready CBDC exists as of 2025.
  This project uses existing stablecoins (USDC/USDT) as
  proxies for future CBDCs:

  ECB_SANDBOX  → Digital Euro (future) — proxy: USDC
  FED_SANDBOX  → Digital Dollar (future) — proxy: USDT
  BIS_MBRIDGE  → Real BIS project for cross-border CBDC
                 (currently being tested with several Asian central banks)
  INTERNAL     → Internal settlement between wallets

  Sources:
  - BIS mBridge: https://www.bis.org/about/bisih/topics/cbdc/mbridge.htm
  - ECB Digital Euro: https://www.ecb.europa.eu/paym/digital_euro/html/index.en.html
  - Fed Digital Dollar: https://www.federalreserve.gov/cbdc.htm
 */

/**
 * @author Moh Khandan
 * Date: 06/25/2026
 * Time: 5:37 PM
 */
@Getter
@RequiredArgsConstructor
public enum CbdcNetwork {

    /**
     * European Central Bank Sandbox
     * Real-world: Digital Euro project in the research phase (2024–2025)
     * Proxy in this sandbox: USDC
     */
    ECB_SANDBOX(
            "European Central Bank Digital Euro Sandbox",
            Currency.USDC,
            "ecb-sandbox-001"
    ),

    /**
     * Federal Reserve Sandbox
     * Real-world: FedNow (instant settlement) + CBDC research
     * Proxy in this sandbox: USDT
     */
    FED_SANDBOX(
            "Federal Reserve Digital Dollar Sandbox",
            Currency.USDT,
            "fed-sandbox-001"
    ),

    /**
     * BIS mBridge — real cross-border CBDC project
     * Real participants: HKMA, PBoC, BOT, CBUAE
     * In this sandbox: multi-currency cross-network swap
     */
    BIS_MBRIDGE(
            "BIS mBridge Cross-Border Multi-CBDC Network",
            null,
            "mbridge-001"
    ),

    /**
     * Internal — settlement between wallets without an external network
     */
    INTERNAL(
            "Internal Settlement Network",
            null,
            "internal-001"
    );

    private final String   displayName;

    /** Native currency of this network (null means multi-currency) */
    private final Currency nativeCurrency;
    private final String   networkCode;
}
