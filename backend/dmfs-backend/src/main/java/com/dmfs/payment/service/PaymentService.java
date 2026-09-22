package com.dmfs.payment.service;

import com.dmfs.client.entity.Client;
import com.dmfs.client.service.CustomerContextService;
import com.dmfs.payment.entity.Payment;
import com.dmfs.payment.provider.PesaPalClient;
import com.dmfs.payment.repository.PaymentRepository;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceRequestRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerContextService customerContextService;
    private final PesaPalClient pesaPalClient;

    private final String currency;
    private final String frontendUrl;

    public PaymentService(
            PaymentRepository paymentRepository,
            ServiceRequestRepository serviceRequestRepository,
            CustomerContextService customerContextService,
            PesaPalClient pesaPalClient,
            @Value("${pesapal.currency:TZS}") String currency,
            @Value("${pesapal.frontend-url:http://localhost:4200}") String frontendUrl
    ) {
        this.paymentRepository = paymentRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.customerContextService = customerContextService;
        this.pesaPalClient = pesaPalClient;
        this.currency = currency;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public PaymentResponse initiate(
            Long serviceRequestId
    ) {

        Client client =
                customerContextService.currentClient();

        ServiceRequest request =
                serviceRequestRepository
                        .findByIdAndCustomerId(
                                serviceRequestId,
                                client.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Service request not found"
                                )
                        );

        if ("PAID".equalsIgnoreCase(
                request.getPaymentStatus()
        )) {

            throw new RuntimeException(
                    "This service request is already paid"
            );
        }

        if (request.getAmount() == null
                || request.getAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "This service request has no payable amount"
            );
        }

        Payment existing =
                paymentRepository
                        .findTopByServiceRequestIdOrderByCreatedAtDesc(
                                request.getId()
                        )
                        .orElse(null);

        if (existing != null
                && ("INITIATED".equals(existing.getStatus())
                || "PENDING".equals(existing.getStatus()))
                && existing.getRedirectUrl() != null
                && !existing.getRedirectUrl().isBlank()) {

            return toResponse(existing);
        }

        Payment payment =
                new Payment();

        payment.setServiceRequest(request);
        payment.setAmount(request.getAmount());
        payment.setCurrency(currency);

        String reference =
                "DMFS-" +
                request.getId() +
                "-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12);

        payment.setMerchantReference(reference);
        payment.setStatus("INITIATED");

        payment =
                paymentRepository.save(payment);

        String email =
                customerContextService
                        .currentUser()
                        .getEmail();

        String firstName =
                customerContextService
                        .currentUser()
                        .getFirstName();

        String lastName =
                customerContextService
                        .currentUser()
                        .getLastName();

        String result =
                pesaPalClient.submitOrder(
                        reference,
                        payment.getAmount(),
                        "DMFS service request #" + request.getId(),
                        email,
                        firstName,
                        lastName
                );

        String[] parts =
                result.split("\n", 2);

        String redirectUrl =
                parts[0];

        String trackingId =
                parts.length > 1
                        ? parts[1]
                        : null;

        payment.setRedirectUrl(redirectUrl);
        payment.setProviderTrackingId(trackingId);
        payment.setStatus("PENDING");

        payment =
                paymentRepository.save(payment);

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse refreshForCustomer(
            Long paymentId
    ) {

        Client client =
                customerContextService.currentClient();

        Payment payment =
                paymentRepository
                        .findByIdAndServiceRequestCustomerId(
                                paymentId,
                                client.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        refresh(payment);

        return toResponse(payment);
    }

    @Transactional
    public void processNotification(
            String trackingId,
            String merchantReference
    ) {

        if (trackingId == null
                || trackingId.isBlank()
                || merchantReference == null
                || merchantReference.isBlank()) {

            throw new RuntimeException(
                    "Invalid PesaPal notification"
            );
        }

        Payment payment =
                paymentRepository
                        .findByMerchantReference(
                                merchantReference
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment reference not found"
                                )
                        );

        if (payment.getProviderTrackingId() == null) {
            payment.setProviderTrackingId(trackingId);
        }

        if (!trackingId.equals(
                payment.getProviderTrackingId()
        )) {

            throw new RuntimeException(
                    "PesaPal tracking ID mismatch"
            );
        }

        refresh(payment);
    }

    @Transactional
    public PaymentResponse getPublicCallbackStatus(
            String trackingId,
            String merchantReference
    ) {

        processNotification(
                trackingId,
                merchantReference
        );

        Payment payment =
                paymentRepository
                        .findByMerchantReference(
                                merchantReference
                        )
                        .orElseThrow();

        return toResponse(payment);
    }

    private void refresh(Payment payment) {

        if (payment.getProviderTrackingId() == null
                || payment.getProviderTrackingId().isBlank()) {

            throw new RuntimeException(
                    "Payment has no PesaPal tracking ID"
            );
        }

        JsonNode response =
                pesaPalClient.getTransactionStatus(
                        payment.getProviderTrackingId()
                );

        String merchantReference =
                response.path("merchant_reference")
                        .asText("");

        if (!payment.getMerchantReference()
                .equals(merchantReference)) {

            throw new RuntimeException(
                    "PesaPal merchant reference mismatch"
            );
        }

        BigDecimal providerAmount =
                response.path("amount")
                        .decimalValue();

        if (providerAmount.compareTo(
                payment.getAmount()
        ) != 0) {

            throw new RuntimeException(
                    "PesaPal amount mismatch"
            );
        }

        String providerCurrency =
                response.path("currency")
                        .asText("");

        if (!payment.getCurrency()
                .equalsIgnoreCase(providerCurrency)) {

            throw new RuntimeException(
                    "PesaPal currency mismatch"
            );
        }

        String status =
                response.path(
                        "payment_status_description"
                )
                .asText("")
                .toUpperCase();

        payment.setPaymentMethod(
                response.path("payment_method")
                        .asText(null)
        );

        payment.setPaymentAccount(
                response.path("payment_account")
                        .asText(null)
        );

        payment.setProviderConfirmationCode(
                response.path("confirmation_code")
                        .asText(null)
        );

        if ("COMPLETED".equals(status)) {

            payment.setStatus("PAID");
            payment.setPaidAt(
                    LocalDateTime.now()
            );

            ServiceRequest request =
                    payment.getServiceRequest();

            request.setPaymentStatus("PAID");
            request.setPaidAt(
                    LocalDateTime.now()
            );

            serviceRequestRepository.save(request);

        } else if ("FAILED".equals(status)
                || "INVALID".equals(status)) {

            payment.setStatus("FAILED");

            payment.setFailureReason(
                    response.path("description")
                            .asText("Payment failed")
            );

        } else if ("REVERSED".equals(status)) {

            payment.setStatus("REVERSED");

            payment.setFailureReason(
                    response.path("description")
                            .asText("Payment reversed")
            );

        } else {

            payment.setStatus("PENDING");
        }

        paymentRepository.save(payment);
    }

    public PaymentResponse toResponse(
            Payment payment
    ) {

        return new PaymentResponse(
                payment.getId(),
                payment.getServiceRequest()
                        .getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getProvider(),
                payment.getMerchantReference(),
                payment.getProviderTrackingId(),
                payment.getProviderConfirmationCode(),
                payment.getPaymentMethod(),
                payment.getRedirectUrl(),
                payment.getFailureReason(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }

    public record PaymentResponse(
            Long id,
            Long serviceRequestId,
            BigDecimal amount,
            String currency,
            String status,
            String provider,
            String merchantReference,
            String providerTrackingId,
            String confirmationCode,
            String paymentMethod,
            String redirectUrl,
            String failureReason,
            LocalDateTime paidAt,
            LocalDateTime createdAt
    ) {
    }
}
