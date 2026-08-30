package com.dmfs.customer.controller;

import com.dmfs.customer.dto.CustomerResponse;
import com.dmfs.customer.entity.Customer;
import com.dmfs.customer.entity.CustomerStatus;
import com.dmfs.customer.entity.CustomerType;
import com.dmfs.customer.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Register new customer
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CreateCustomerRequest request
    ) {

        Customer customer = customerService.create(
                request.customerCode(),
                request.type(),
                request.companyName(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.address(),
                request.identificationNumber(),
                request.tin(),
                request.password()
        );

        return ResponseEntity.ok(
                CustomerResponse.from(customer)
        );
    }

    // Get all customers
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<List<CustomerResponse>> findAll() {

        List<CustomerResponse> customers =
                customerService.findAll()
                        .stream()
                        .map(CustomerResponse::from)
                        .toList();

        return ResponseEntity.ok(customers);
    }

    // Get customer by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<CustomerResponse> findById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                CustomerResponse.from(
                        customerService.findById(id)
                )
        );
    }

    // Update customer
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {

        Customer customer = customerService.update(
                id,
                request.type(),
                request.companyName(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.address(),
                request.identificationNumber(),
                request.tin(),
                request.status()
        );

        return ResponseEntity.ok(
                CustomerResponse.from(customer)
        );
    }

    // Delete customer
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        customerService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // ============================
    // CREATE CUSTOMER REQUEST
    // ============================

    public record CreateCustomerRequest(

            @NotBlank
            String customerCode,

            @NotNull
            CustomerType type,

            String companyName,

            String firstName,

            String lastName,

            @NotBlank
            @Email
            String email,

            String phone,

            String address,

            String identificationNumber,

            String tin,

            @NotBlank
            String password
    ) {
    }

    // ============================
    // UPDATE CUSTOMER REQUEST
    // ============================

    public record UpdateCustomerRequest(

            @NotNull
            CustomerType type,

            String companyName,

            String firstName,

            String lastName,

            @NotBlank
            @Email
            String email,

            String phone,

            String address,

            String identificationNumber,

            String tin,

            @NotNull
            CustomerStatus status
    ) {
    }
}
