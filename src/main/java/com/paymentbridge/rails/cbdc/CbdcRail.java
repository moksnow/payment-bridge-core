package com.paymentbridge.rails.cbdc;

import com.paymentbridge.common.enums.CbdcNetwork;
import com.paymentbridge.common.enums.CbdcOperationType;
import com.paymentbridge.common.enums.Currency;
import com.paymentbridge.common.enums.PaymentRailType;
import com.paymentbridge.exception.RailException;
import com.paymentbridge.payment.entity.Payment;
import com.paymentbridge.rails.PaymentRail;
import com.paymentbridge.rails.RailResult;
import com.paymentbridge.rails.cbdc.dto.CbdcSettlementRequest;
import com.paymentbridge.rails.cbdc.dto.CbdcSettlementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.EnumSet;
import java.util.Set;

/*
 * CBDC Rail — connection to the CBDC sandbox network.
 * <p>
 * Messages follow ISO 20022:
 * - Request:  pacs.008 (Credit Transfer)
 * - Response: pacs.002 (Payment Status Report)
 * <p>
 * Bridge Logic (via CbdcBridgeResolver):
 * USD  → USDC : MINT     — fiat enters the network
 * USDC → USD  : REDEEM   — exits the network
 * USDC → USDC : TRANSFER — within the same network
 * USDC → USDT : SWAP     — cross-network
 * <p>
 * Lifecycle:
 * PDNG → ACCP → STLD  (success)
 * PDNG → RJCT         (failure)
 */

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CbdcRail implements PaymentRail {

    private static final Set<Currency> SUPPORTED_CURRENCIES = EnumSet.of(
            Currency.USDC,
            Currency.USDT,
            Currency.USD,
            Currency.EUR,
            Currency.GBP
    );

    private final CbdcProperties cbdcProperties;
    private final CbdcBridgeResolver bridgeResolver;
    private final RestTemplate restTemplate;

    @Override
    public PaymentRailType railType() {
        return PaymentRailType.CBDC_SANDBOX;
    }

    @Override
    public boolean supports(Payment payment) {
        return payment.getRailType() == PaymentRailType.CBDC_SANDBOX
                && SUPPORTED_CURRENCIES.contains(payment.getCurrency());
    }

    @Override
    public RailResult process(Payment payment) {

        // ── Determine bridge logic ──
        Currency fromCcy = payment.getCurrency();
        Currency toCcy = payment.getReceiveCurrency() != null
                ? payment.getReceiveCurrency()
                : payment.getCurrency();
        CbdcOperationType opType = bridgeResolver.resolveOperation(fromCcy, toCcy);
        CbdcNetwork network = bridgeResolver.resolveNetwork(fromCcy, toCcy);
        boolean isCross = bridgeResolver.isCrossNetwork(payment);

        log.info("CbdcRail processing payment=[{}] op=[{}] network=[{}] crossNetwork=[{}]",
                payment.getId(), opType, network, isCross);

        // ── Build ISO 20022 pacs.008 message ──
        CbdcSettlementRequest request = CbdcSettlementRequest.builder()
                .msgId(payment.getIdempotencyKey())
                .instrId(payment.getId())
                .dbtrAcct(payment.getSenderWalletAccount())
                .cdtrAcct(payment.getReceiverWalletAccount())
                .instdAmt(payment.getAmount())
                .ccy(fromCcy.name())
                .purp(opType)
                .network(network)
                .networkId(cbdcProperties.getNetworkId())
                .rmtInf(payment.getDescription())
                .build();

        try {
            String url = cbdcProperties.getSandboxUrl() + "/cbdc-sandbox/settle";
            CbdcSettlementResponse response = restTemplate.postForObject(
                    url, request, CbdcSettlementResponse.class);

            if (response == null) {
                throw new RailException(PaymentRailType.CBDC_SANDBOX,
                        "No response from CBDC sandbox");
            }

            log.info("CbdcRail response status=[{}] txId=[{}]",
                    response.getStatus(), response.getTxId());

            // PDNG — sandbox confirms immediately,
            // but in production this would be asynchronous.
            if (response.isPending()) {
                log.info("CbdcRail PDNG — sandbox auto-confirms immediately");
            }

            if (response.isAccepted() || response.isSettled()) {
                return RailResult.success(response.getTxId());
            }

            if (response.isRejected()) {
                return RailResult.failure("CBDC_RJCT", response.getRsn());
            }

            return RailResult.failure("CBDC_UNKNOWN",
                    "Unknown status: " + response.getStatus());

        } catch (RailException e) {
            throw e;
        } catch (Exception e) {
            log.error("CbdcRail error payment=[{}]: {}", payment.getId(), e.getMessage());
            throw new RailException(PaymentRailType.CBDC_SANDBOX, e.getMessage(), e);
        }
    }
}
