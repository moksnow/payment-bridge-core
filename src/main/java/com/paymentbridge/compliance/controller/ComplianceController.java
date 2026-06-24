package com.paymentbridge.compliance.controller;

import com.paymentbridge.compliance.dto.AmlFlagResponse;
import com.paymentbridge.compliance.dto.KycProfileResponse;
import com.paymentbridge.compliance.dto.UpdateKycRequest;
import com.paymentbridge.compliance.service.AmlService;
import com.paymentbridge.compliance.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Moh Khandan
 * Date: 06/24/2026
 * Time: 10:01 PM
 */
@RestController
@RequestMapping("/v1/compliance")
@RequiredArgsConstructor
@Tag(name = "Compliance", description = "KYC and AML management")
@SecurityRequirement(name = "bearerAuth")
public class ComplianceController {

    private final KycService kycService;
    private final AmlService amlService;

    @GetMapping("/kyc/me")
    @Operation(summary = "Get my KYC profile")
    public ResponseEntity<KycProfileResponse> getMyKyc() {
        String userId = currentUserId();
        return ResponseEntity.ok(kycService.getProfile(userId));
    }

    @PutMapping("/kyc/{userId}")
    @Operation(summary = "Update user KYC level (admin)")
    public ResponseEntity<KycProfileResponse> updateKyc(
            @PathVariable String userId,
            @Valid @RequestBody UpdateKycRequest req) {
        String adminId = currentUserId();
        return ResponseEntity.ok(kycService.updateLevel(userId, req, adminId));
    }

    @GetMapping("/aml/me")
    @Operation(summary = "Get my AML flags")
    public ResponseEntity<List<AmlFlagResponse>> getMyFlags() {
        return ResponseEntity.ok(amlService.getFlagsForUser(currentUserId()));
    }

    @GetMapping("/aml/flagged")
    @Operation(summary = "Get all flagged transactions (admin)")
    public ResponseEntity<List<AmlFlagResponse>> getFlagged() {
        return ResponseEntity.ok(amlService.getFlagged());
    }

    private String currentUserId() {
        return (String) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }
}
