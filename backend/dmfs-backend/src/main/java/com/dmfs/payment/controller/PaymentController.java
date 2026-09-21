package com.dmfs.payment.controller;

import com.dmfs.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }

    @PostMapping("/customer/payments/service-requests/{serviceRequestId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> initiate(
            @PathVariable Long serviceRequestId
    ) {

        return ResponseEntity.ok(
                paymentService.initiate(
                        serviceRequestId
                )
        );
    }

    @GetMapping("/customer/payments/{paymentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> status(
            @PathVariable Long paymentId
    ) {

        return ResponseEntity.ok(
                paymentService.refreshForCustomer(
                        paymentId
                )
        );
    }

    @GetMapping("/payments/pesapal/callback")
    public ResponseEntity<?> callback(
            @RequestParam(
                    name = "OrderTrackingId",
                    required = false
            )
            String trackingId,

            @RequestParam(
                    name = "OrderMerchantReference",
                    required = false
            )
            String merchantReference
    ) {

        try {

            PaymentService.PaymentResponse payment =
                    paymentService.getPublicCallbackStatus(
                            trackingId,
                            merchantReference
                    );

            return ResponseEntity
                    .status(302)
                    .header(
                            "Location",
                            "/customer/payments/return?paymentId="
                                    + payment.id()
                    )
                    .build();

        } catch (Exception e) {

            return ResponseEntity
                    .status(302)
                    .header(
                            "Location",
                            "/customer/payments/return?status=error"
                    )
                    .build();
        }
    }

    @GetMapping("/payments/pesapal/ipn")
    public ResponseEntity<?> ipn(
            @RequestParam(
                    name = "OrderTrackingId",
                    required = false
            )
            String trackingId,

            @RequestParam(
                    name = "OrderMerchantReference",
                    required = false
            )
            String merchantReference
    ) {

        try {

            paymentService.processNotification(
                    trackingId,
                    merchantReference
            );

            return ResponseEntity.ok(
                    Map.of(
                            "orderNotificationType",
                            "IPNCHANGE",
                            "orderTrackingId",
                            trackingId,
                            "orderMerchantReference",
                            merchantReference,
                            "status",
                            200
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "status",
                                    500,
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}
