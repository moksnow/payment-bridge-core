package com.paymentbridge.common.constants;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
public final class AppConstants {

    public static final String API_V1 = "/v1";
    public static final String AUTH_PATH = API_V1 + "/auth";
    public static final String PAYMENTS_PATH = API_V1 + "/payments";
    public static final String LEDGER_PATH = API_V1 + "/ledger";
    public static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ACCOUNT_SUSPENSE = "SYS-SUSPENSE";
    public static final String ACCOUNT_FEE = "SYS-FEE";

    private AppConstants() {
    }
}
