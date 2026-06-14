package com.paymentbridge.payment.repository;

import com.paymentbridge.common.enums.PaymentStatus;
import com.paymentbridge.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findBySenderAccount(String senderAccount);
}
