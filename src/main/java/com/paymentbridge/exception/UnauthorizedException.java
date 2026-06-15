/**
 * @author Moh Khandan
 * Date: 5/1/2026
 * Time: 5:37 PM
 */
package com.paymentbridge.exception;

import org.springframework.http.HttpStatus;

/*
 * @author Moh Khandan
 * Date: 06/15/2026
 * Time: 17:27 PM
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }
}
