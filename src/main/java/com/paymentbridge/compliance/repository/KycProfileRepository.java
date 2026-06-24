package com.paymentbridge.compliance.repository;

import com.paymentbridge.compliance.entity.KycProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Moh Khandan
 * Date: 06/24/2026
 * Time: 10:01 PM
 */
@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, String> {
    Optional<KycProfile> findByUserId(String userId);
}
