package com.paymentbridge.payment.controller;

import com.paymentbridge.common.constants.AppConstants;
import com.paymentbridge.payment.dto.CreatePaymentRequest;
import com.paymentbridge.payment.dto.PaymentResponse;
import com.paymentbridge.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@RestController
@RequestMapping(AppConstants.PAYMENTS_PATH)
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment Bridge core API")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Initiate a new payment")
    public ResponseEntity<PaymentResponse> initiate(
            @Valid @RequestBody CreatePaymentRequest req,
            @RequestHeader(AppConstants.IDEMPOTENCY_HEADER) String idempotencyKey) {

        PaymentResponse response = paymentService.initiate(req, idempotencyKey);
        HttpStatus status = switch (response.getStatus()) {
            case COMPLETED -> HttpStatus.CREATED;
            case FAILED    -> HttpStatus.UNPROCESSABLE_ENTITY;
            default        -> HttpStatus.ACCEPTED;
        };
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get my payments")
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }
}
