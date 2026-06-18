package com.paymentbridge.rails;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
public class RailResult {

    private final boolean success;
    private final String externalRef;
    private final String failureCode;
    private final String failureMessage;

    private RailResult(boolean success, String externalRef,
                       String failureCode, String failureMessage) {
        this.success = success;
        this.externalRef = externalRef;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
    }

    public static RailResult success(String externalRef) {
        return new RailResult(true, externalRef, null, null);
    }

    public static RailResult failure(String code, String message) {
        return new RailResult(false, null, code, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public String getFailureMessage() {
        return failureMessage;
    }
}
