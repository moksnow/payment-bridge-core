package com.paymentbridge.rails.cbdc.dto;

import com.paymentbridge.common.enums.CbdcTxStatus;
import lombok.Data;

import java.time.Instant;

/*
 * Response returned by a CBDC network after settlement.
 * Based on ISO 20022 — pacs.002 (FI to FI Payment Status Report).
 *
 * ISO 20022 field mapping:
 * txId       → TxInfAndSts/OrgnlInstrId
 * status     → TxInfAndSts/TxSts (ACCP/RJCT/PDNG/STLD)
 * rsn        → TxInfAndSts/StsRsnInf/Rsn/Cd
 * networkRef → TxInfAndSts/OrgnlTxRef/MsgId
 * stldAt     → TxInfAndSts/OrgnlTxRef/IntrBkSttlmDt
 */

/**
 * @author Moh Khandan
 * Date: 06/25/2026
 * Time: 5:37 PM
 */
@Data
public class CbdcSettlementResponse {

    /**
     * ISO 20022: TxInfAndSts/OrgnlInstrId
     */
    private String txId;

    /**
     * ISO 20022: TxInfAndSts/TxSts
     */
    private CbdcTxStatus status;

    /**
     * ISO 20022: TxInfAndSts/StsRsnInf/Rsn/Cd
     */
    private String rsn;

    /**
     * ISO 20022: TxInfAndSts/OrgnlTxRef/MsgId
     */
    private String networkRef;

    /**
     * Final settlement timestamp
     */
    private Instant stldAt;

    public boolean isAccepted() {
        return CbdcTxStatus.ACCP == status;
    }

    public boolean isSettled() {
        return CbdcTxStatus.STLD == status;
    }

    public boolean isRejected() {
        return CbdcTxStatus.RJCT == status;
    }

    public boolean isPending() {
        return CbdcTxStatus.PDNG == status;
    }
}
