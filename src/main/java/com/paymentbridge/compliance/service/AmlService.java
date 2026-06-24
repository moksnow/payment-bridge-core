package com.paymentbridge.compliance.service;

import com.paymentbridge.common.enums.AmlStatus;
import com.paymentbridge.common.enums.KycLevel;
import com.paymentbridge.compliance.dto.AmlFlagResponse;
import com.paymentbridge.compliance.entity.AmlFlag;
import com.paymentbridge.compliance.entity.KycProfile;
import com.paymentbridge.compliance.repository.AmlFlagRepository;
import com.paymentbridge.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author Moh Khandan
 * Date: 06/24/2026
 * Time: 10:01 PM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmlService {

    private final AmlFlagRepository amlFlagRepository;
    private final KycService kycService;

    @Transactional
    public void check(String userId, BigDecimal amount, String paymentId) {
        KycProfile profile = kycService.getOrCreate(userId);

        // ── Rule 1: Daily limit ──
        BigDecimal todayTotal = getDailyTotal(userId);
        BigDecimal dailyLimit = profile.getKycLevel().getDailyLimit();

        if (todayTotal.add(amount).compareTo(dailyLimit) > 0) {
            AmlFlag flag = saveFlag(paymentId, userId, amount,
                    AmlStatus.BLOCKED,
                    "Daily limit exceeded — limit: " + dailyLimit
                            + " used: " + todayTotal + " requested: " + amount);
            log.warn("AML BLOCK daily limit userId=[{}] flag=[{}]", userId, flag.getId());
            throw new BusinessException("AML_DAILY_LIMIT",
                    "Daily transaction limit exceeded for your KYC level",
                    HttpStatus.FORBIDDEN);
        }

        if (isRoundNumber(amount) && amount.compareTo(new BigDecimal("5000")) >= 0) {
            AmlFlag flag = saveFlag(paymentId, userId, amount,
                    AmlStatus.FLAGGED,
                    "Large round number transaction — possible structuring");
            log.warn("AML FLAG structuring suspected userId=[{}] amount=[{}] flag=[{}]",
                    userId, amount, flag.getId());
        }

        if (profile.getKycLevel() == KycLevel.UNVERIFIED
                && amount.compareTo(new BigDecimal("50")) > 0) {
            log.info("AML INFO unverified user high amount userId=[{}] amount=[{}]", userId, amount);
        }

        saveFlag(paymentId, userId, amount, AmlStatus.CLEAR, "Transaction cleared");
    }

    @Transactional(readOnly = true)
    public List<AmlFlagResponse> getFlagsForUser(String userId) {
        return amlFlagRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AmlFlagResponse> getFlagged() {
        return amlFlagRepository.findByStatus(AmlStatus.FLAGGED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BigDecimal getDailyTotal(String userId) {
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return amlFlagRepository.sumAmountSince(userId, startOfDay, AmlStatus.CLEAR);
    }

    private boolean isRoundNumber(BigDecimal amount) {
        return amount.remainder(new BigDecimal("1000"))
                .compareTo(BigDecimal.ZERO) == 0;
    }

    private AmlFlag saveFlag(String paymentId, String userId,
                             BigDecimal amount, AmlStatus status, String reason) {
        AmlFlag flag = AmlFlag.builder()
                .paymentId(paymentId)
                .userId(userId)
                .amount(amount)
                .status(status)
                .reason(reason)
                .build();
        return amlFlagRepository.save(flag);
    }

    private AmlFlagResponse toResponse(AmlFlag f) {
        return AmlFlagResponse.builder()
                .id(f.getId())
                .paymentId(f.getPaymentId())
                .userId(f.getUserId())
                .status(f.getStatus())
                .reason(f.getReason())
                .amount(f.getAmount())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
