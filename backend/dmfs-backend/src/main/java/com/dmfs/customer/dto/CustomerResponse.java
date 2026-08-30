package com.dmfs.customer.dto;

import com.dmfs.customer.entity.Customer;
import com.dmfs.customer.entity.CustomerStatus;
import com.dmfs.customer.entity.CustomerType;

public record CustomerResponse(
        Long id,
        String customerCode,
        CustomerType type,
        String companyName,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String identificationNumber,
        String tin,
        CustomerStatus status
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getType(),
                customer.getCompanyName(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getIdentificationNumber(),
                customer.getTin(),
                customer.getStatus()
        );
    }
}