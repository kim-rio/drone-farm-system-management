package com.dmfs.payment.repository;

import com.dmfs.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMerchantReference(
            String merchantReference
    );

    Optional<Payment> findByIdAndServiceRequestCustomerId(
            Long id,
            Long clientId
    );

    Optional<Payment> findTopByServiceRequestIdOrderByCreatedAtDesc(
            Long serviceRequestId
    );
}
