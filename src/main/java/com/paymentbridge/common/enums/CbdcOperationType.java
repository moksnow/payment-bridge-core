package com.paymentbridge.common.enums;

/*
  Type of CBDC operation.
  Based on ISO 20022 — CdtTrfTxInf/Purp/Cd
  <p>
  MINT     → Fiat  → CBDC  (deposit into the network)
  REDEEM   → CBDC  → Fiat  (withdrawal from the network)
  TRANSFER → CBDC  → CBDC  (transfer within the same network)
  SWAP     → CBDC  → CBDC  (between two different networks — cross-network)
 */

/**
 * @author Moh Khandan
 * Date: 06/25/2026
 * Time: 5:37 PM
 */
public enum CbdcOperationType {
    MINT,
    REDEEM,
    TRANSFER,
    SWAP
}
