package com.paymentbridge.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
public class InsufficientFundsException extends BusinessException {

    public InsufficientFundsException(String account) {
        super("INSUFFICIENT_FUNDS",
              "Insufficient funds for account: " + account,
              HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
