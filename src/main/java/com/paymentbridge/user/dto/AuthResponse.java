/**
 * @author Moh Khandan
 * Date: 5/1/2026
 * Time: 5:37 PM
 */
package com.paymentbridge.user.dto;

import lombok.Builder;
import lombok.Data;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Data
@Builder
public class AuthResponse {

    private String token;
    private String userId;
    private String email;
    private long expiresIn;
}
