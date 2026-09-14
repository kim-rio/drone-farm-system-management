package com.dmfs.service.controller;

import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/management/payments")
@PreAuthorize("hasRole('MANAGEMENT')")
public class PaymentController {

    private final ServiceRequestRepository serviceRequestRepository;

    public PaymentController(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @PutMapping("/{id}/paid")
    public ResponseEntity<ServiceRequest> markPaid(@PathVariable Long id) {
        ServiceRequest request = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        request.setPaymentStatus("PAID");
        request.setPaidAt(LocalDateTime.now());

        return ResponseEntity.ok(serviceRequestRepository.save(request));
    }
}
