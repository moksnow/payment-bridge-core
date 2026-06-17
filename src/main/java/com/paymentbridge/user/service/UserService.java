package com.paymentbridge.user.service;

import com.paymentbridge.exception.BusinessException;
import com.paymentbridge.security.JwtProvider;
import com.paymentbridge.user.dto.AuthResponse;
import com.paymentbridge.user.dto.LoginRequest;
import com.paymentbridge.user.dto.RegisterRequest;
import com.paymentbridge.user.entity.User;
import com.paymentbridge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("EMAIL_TAKEN",
                    "Email already registered: " + req.getEmail(),
                    HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        String token = jwtProvider.generate(user.getId(), user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .expiresIn(jwtProvider.getExpirationMs() / 1000)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS",
                        "Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS",
                    "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        if (!user.isActive()) {
            throw new BusinessException("USER_INACTIVE",
                    "User account is inactive", HttpStatus.FORBIDDEN);
        }

        log.info("User logged in: {}", user.getEmail());

        String token = jwtProvider.generate(user.getId(), user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .expiresIn(jwtProvider.getExpirationMs() / 1000)
                .build();
    }
}
