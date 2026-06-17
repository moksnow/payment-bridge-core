package com.paymentbridge.user.repository;

import com.paymentbridge.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
