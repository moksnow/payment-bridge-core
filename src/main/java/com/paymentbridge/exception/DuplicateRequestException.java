package com.paymentbridge.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
public class DuplicateRequestException extends BusinessException {

    public DuplicateRequestException(String idempotencyKey) {
        super("DUPLICATE_REQUEST",
              "Request already processed: " + idempotencyKey,
              HttpStatus.CONFLICT);
    }
}
