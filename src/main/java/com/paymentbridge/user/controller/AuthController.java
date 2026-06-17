/**
 * @author Moh Khandan
 * Date: 5/1/2026
 * Time: 5:37 PM
 */
package com.paymentbridge.user.controller;

import com.paymentbridge.user.dto.AuthResponse;
import com.paymentbridge.user.dto.LoginRequest;
import com.paymentbridge.user.dto.RegisterRequest;
import com.paymentbridge.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Register and login")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }
}
