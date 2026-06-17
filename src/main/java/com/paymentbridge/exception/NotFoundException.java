package com.paymentbridge.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String resource, String id) {
        super("NOT_FOUND",
              resource + " not found: " + id,
              HttpStatus.NOT_FOUND);
    }
}
