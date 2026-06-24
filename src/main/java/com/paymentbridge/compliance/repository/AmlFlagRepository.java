package com.paymentbridge.compliance.repository;

import com.paymentbridge.common.enums.AmlStatus;
import com.paymentbridge.compliance.entity.AmlFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * @author Moh Khandan
 * Date: 06/24/2026
 * Time: 10:01 PM
 */
@Repository
public interface AmlFlagRepository extends JpaRepository<AmlFlag, String> {

    List<AmlFlag> findByUserId(String userId);

    List<AmlFlag> findByStatus(AmlStatus status);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM AmlFlag f " +
            "WHERE f.userId = :userId AND f.createdAt >= :since AND f.status = :status")
    BigDecimal sumAmountSince(String userId, Instant since,
                              @org.springframework.data.repository.query.Param("status") AmlStatus status);
}
