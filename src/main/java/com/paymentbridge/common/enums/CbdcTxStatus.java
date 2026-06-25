package com.paymentbridge.common.enums;

/*
  Transaction status within a CBDC network.
  Based on ISO 20022 — pacs.002 TxSts codes.

  PDNG → ACCP → STLD   (successful path)
  PDNG → RJCT          (failure path)
 */

/**
 * @author Moh Khandan
 * Date: 06/25/2026
 * Time: 5:37 PM
 */
public enum CbdcTxStatus {

    /** PDNG — Pending: waiting for network approval */
    PDNG,

    /** ACCP — Accepted: accepted by the network, settlement in progress */
    ACCP,

    /** STLD — Settled: finalized and irreversible */
    STLD,

    /** RJCT — Rejected: transaction rejected */
    RJCT
}
