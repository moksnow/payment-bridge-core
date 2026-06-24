package com.paymentbridge.compliance.service;

import com.paymentbridge.common.enums.KycLevel;
import com.paymentbridge.compliance.dto.KycProfileResponse;
import com.paymentbridge.compliance.dto.UpdateKycRequest;
import com.paymentbridge.compliance.entity.KycProfile;
import com.paymentbridge.compliance.repository.KycProfileRepository;
import com.paymentbridge.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author Moh Khandan
 * Date: 06/24/2026
 * Time: 10:01 PM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycProfileRepository kycProfileRepository;

    @Transactional
    public KycProfile getOrCreate(String userId) {
        return kycProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    KycProfile profile = KycProfile.builder()
                            .userId(userId)
                            .kycLevel(KycLevel.UNVERIFIED)
                            .build();
                    return kycProfileRepository.save(profile);
                });
    }

    @Transactional
    public void assertAllowed(String userId, BigDecimal amount) {
        KycProfile profile = getOrCreate(userId);
        KycLevel level = profile.getKycLevel();

        if (amount.compareTo(level.getMaxTransactionAmount()) > 0) {
            log.warn("KYC block userId=[{}] level=[{}] amount=[{}] max=[{}]",
                    userId, level, amount, level.getMaxTransactionAmount());
            throw new BusinessException("KYC_LIMIT_EXCEEDED",
                    "Transaction amount exceeds your KYC level limit of "
                            + level.getMaxTransactionAmount() + " — current level: " + level.name(),
                    HttpStatus.FORBIDDEN);
        }
    }

    @Transactional
    public KycProfileResponse getProfile(String userId) {
        KycProfile profile = getOrCreate(userId);
        return toResponse(profile);
    }

    @Transactional
    public KycProfileResponse updateLevel(String userId, UpdateKycRequest req, String adminId) {
        KycProfile profile = getOrCreate(userId);
        profile.setKycLevel(req.getKycLevel());
        profile.setNotes(req.getNotes());
        profile.setReviewedBy(adminId);
        kycProfileRepository.save(profile);
        log.info("KYC updated userId=[{}] level=[{}] by admin=[{}]", userId, req.getKycLevel(), adminId);
        return toResponse(profile);
    }

    private KycProfileResponse toResponse(KycProfile p) {
        return KycProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .kycLevel(p.getKycLevel())
                .maxTransactionAmount(p.getKycLevel().getMaxTransactionAmount())
                .dailyLimit(p.getKycLevel().getDailyLimit())
                .notes(p.getNotes())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
