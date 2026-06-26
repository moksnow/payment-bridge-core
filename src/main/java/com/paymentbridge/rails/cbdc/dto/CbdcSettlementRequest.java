package com.paymentbridge.rails.cbdc.dto;

import com.paymentbridge.common.enums.CbdcNetwork;
import com.paymentbridge.common.enums.CbdcOperationType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/*
 * Settlement request sent to a CBDC network.
 * Based on ISO 20022 — pacs.008 (FI to FI Customer Credit Transfer).
 *
 * ISO 20022 field mapping:
 * msgId    → GrpHdr/MsgId
 * creDtTm  → GrpHdr/CreDtTm
 * instrId  → CdtTrfTxInf/PmtId/InstrId
 * dbtrAcct → CdtTrfTxInf/DbtrAcct/Id
 * cdtrAcct → CdtTrfTxInf/CdtrAcct/Id
 * instdAmt → CdtTrfTxInf/InstdAmt
 * ccy      → CdtTrfTxInf/InstdAmt/@Ccy
 * purp     → CdtTrfTxInf/Purp/Cd (MINT/REDEEM/TRANSFER/SWAP)
 * rmtInf   → CdtTrfTxInf/RmtInf/Ustrd
 */

/**
 * @author Moh Khandan
 * Date: 06/25/2026
 * Time: 5:37 PM
 */
@Data
@Builder
public class CbdcSettlementRequest {

    /**
     * ISO 20022: GrpHdr/MsgId — idempotency key
     */
    private String msgId;

    /**
     * ISO 20022: GrpHdr/CreDtTm
     */
    @Builder.Default
    private Instant creDtTm = Instant.now();

    /**
     * ISO 20022: CdtTrfTxInf/PmtId/InstrId
     */
    private String instrId;

    /**
     * ISO 20022: CdtTrfTxInf/DbtrAcct/Id
     */
    private String dbtrAcct;

    /**
     * ISO 20022: CdtTrfTxInf/CdtrAcct/Id
     */
    private String cdtrAcct;

    /**
     * ISO 20022: CdtTrfTxInf/InstdAmt
     */
    private BigDecimal instdAmt;

    /**
     * ISO 20022: CdtTrfTxInf/InstdAmt/@Ccy
     */
    private String ccy;

    /**
     * ISO 20022: CdtTrfTxInf/Purp/Cd — MINT/REDEEM/TRANSFER/SWAP
     */
    private CbdcOperationType purp;

    /**
     * Target CBDC network
     */
    private CbdcNetwork network;

    /**
     * Network identifier
     */
    private String networkId;

    /**
     * ISO 20022: CdtTrfTxInf/RmtInf/Ustrd — description
     */
    private String rmtInf;
}
