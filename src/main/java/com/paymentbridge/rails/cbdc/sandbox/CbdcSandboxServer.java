package com.paymentbridge.rails.cbdc.sandbox;

import com.paymentbridge.common.enums.CbdcOperationType;
import com.paymentbridge.common.enums.CbdcTxStatus;
import com.paymentbridge.rails.cbdc.dto.CbdcSettlementRequest;
import com.paymentbridge.rails.cbdc.dto.CbdcSettlementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/*
 * CBDC Sandbox Server — a simulated CBDC network.
 *
 * Responses follow ISO 20022 — pacs.002 using real status enums.
 *
 * Sandbox rules by operation type:
 *
 * MINT     → USD/EUR → USDC/USDT: ACCP
 * REDEEM   → USDC/USDT → USD/EUR: ACCP with simulated delay
 * TRANSFER → Within the same network: immediate STLD
 * SWAP     → Cross-network: ACCP (asynchronous settlement)
 *
 * Error simulation:
 * - rmtInf contains "fail" → RJCT with reason code NARR
 * - amount > 999999        → RJCT with reason code AM09
 */

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Slf4j
@RestController
@RequestMapping("/cbdc-sandbox")
@Tag(name = "CBDC Sandbox", description = "Simulated CBDC network — ISO 20022 pacs.008/pacs.002")
public class CbdcSandboxServer {

    @PostMapping("/settle")
    @Operation(summary = "CBDC settlement — pacs.008 in / pacs.002 out")
    public CbdcSettlementResponse settle(@RequestBody CbdcSettlementRequest request) {

        log.info("CBDC Sandbox received: op=[{}] network=[{}] msgId=[{}] amount=[{} {}]",
                request.getPurp(), request.getNetwork(),
                request.getMsgId(), request.getInstdAmt(), request.getCcy());

        CbdcSettlementResponse response = new CbdcSettlementResponse();

        // ── Simulated rejection: fail keyword ──
        if (request.getRmtInf() != null
                && request.getRmtInf().toLowerCase().contains("fail")) {
            response.setStatus(CbdcTxStatus.RJCT);
            response.setRsn("NARR/Simulated rejection by sandbox");
            log.warn("CBDC Sandbox RJCT msgId=[{}] reason=forced failure", request.getMsgId());
            return response;
        }

        // ── Simulated rejection: amount exceeds limit ──
        if (request.getInstdAmt() != null
                && request.getInstdAmt().compareTo(new java.math.BigDecimal("999999")) > 0) {
            response.setStatus(CbdcTxStatus.RJCT);
            response.setRsn("AM09/Amount exceeds CBDC network settlement limit");
            log.warn("CBDC Sandbox RJCT msgId=[{}] reason=amount limit", request.getMsgId());
            return response;
        }

        // ── Process by operation type ──
        String txId = "CBDC-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String networkRef = (request.getNetworkId() != null ? request.getNetworkId() : "sandbox")
                + "-" + request.getMsgId();

        response.setTxId(txId);
        response.setNetworkRef(networkRef);

        CbdcOperationType op = request.getPurp();

        if (op == CbdcOperationType.TRANSFER) {
            // Transfer within the same network — immediately settled
            response.setStatus(CbdcTxStatus.STLD);
            response.setStldAt(Instant.now());
            log.info("CBDC Sandbox STLD (TRANSFER) txId=[{}]", txId);

        } else if (op == CbdcOperationType.MINT) {
            // Fiat enters the network and is converted into CBDC
            response.setStatus(CbdcTxStatus.ACCP);
            log.info("CBDC Sandbox ACCP (MINT) txId=[{}]", txId);

        } else if (op == CbdcOperationType.REDEEM) {
            // CBDC is redeemed back to fiat (typically async in real systems)
            response.setStatus(CbdcTxStatus.ACCP);
            log.info("CBDC Sandbox ACCP (REDEEM) txId=[{}]", txId);

        } else if (op == CbdcOperationType.SWAP) {
            // Cross-network CBDC swap (requires coordination in real systems)
            response.setStatus(CbdcTxStatus.ACCP);
            log.info("CBDC Sandbox ACCP (SWAP cross-network) txId=[{}]", txId);

        } else {
            // Default behavior
            response.setStatus(CbdcTxStatus.ACCP);
            log.info("CBDC Sandbox ACCP (default) txId=[{}]", txId);
        }

        return response;
    }

    @GetMapping("/status/{txId}")
    @Operation(summary = "Query CBDC transaction status — pacs.002")
    public CbdcSettlementResponse status(@PathVariable String txId) {
        CbdcSettlementResponse response = new CbdcSettlementResponse();
        response.setTxId(txId);
        response.setStatus(CbdcTxStatus.STLD);
        response.setStldAt(Instant.now());
        response.setNetworkRef("sandbox-status-" + txId);
        log.info("CBDC Sandbox status query txId=[{}] → STLD", txId);
        return response;
    }

    @GetMapping("/networks")
    @Operation(summary = "List available CBDC networks")
    public java.util.Map<String, Object> networks() {
        return java.util.Map.of(
                "networks", java.util.List.of(
                        java.util.Map.of("id", "ECB_SANDBOX", "currency", "USDC", "name", "European Central Bank Sandbox"),
                        java.util.Map.of("id", "FED_SANDBOX", "currency", "USDT", "name", "Federal Reserve Sandbox"),
                        java.util.Map.of("id", "BIS_MBRIDGE", "currency", "multi", "name", "BIS mBridge Cross-Border"),
                        java.util.Map.of("id", "INTERNAL", "currency", "all", "name", "Internal Settlement Network")
                ),
                "supportedOperations", java.util.List.of("MINT", "REDEEM", "TRANSFER", "SWAP")
        );
    }
}
