/**
 * @author Moh Khandan
 * Date: 5/1/2026
 * Time: 5:37 PM
 */
package com.paymentbridge.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Data
public class RegisterRequest {

    @NotBlank
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    private String password;
}
