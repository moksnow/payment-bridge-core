package com.paymentbridge.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
public class InvalidPaymentException extends BusinessException {

    public InvalidPaymentException(String message) {
        super("INVALID_PAYMENT", message, HttpStatus.BAD_REQUEST);
    }
}
