package com.dmfs.customer.service;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.customer.entity.Customer;
import com.dmfs.customer.entity.CustomerStatus;
import com.dmfs.customer.entity.CustomerType;
import com.dmfs.customer.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(
            CustomerRepository customerRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Customer create(
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
            String password
    ) {

        if (customerRepository.existsByCustomerCode(customerCode)) {
            throw new RuntimeException("Customer code already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        user = userRepository.save(user);

        Customer customer = new Customer();

        customer.setCustomerCode(customerCode);
        customer.setType(type);
        customer.setCompanyName(companyName);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setIdentificationNumber(identificationNumber);
        customer.setTin(tin);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setUser(user);

        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found")
                );
    }

    @Transactional
    public Customer update(
            Long id,
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

        Customer customer = findById(id);

        if (!customer.getEmail().equalsIgnoreCase(email)
                && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        customer.setType(type);
        customer.setCompanyName(companyName);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setIdentificationNumber(identificationNumber);
        customer.setTin(tin);
        customer.setStatus(status);

        User user = customer.getUser();

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(status == CustomerStatus.ACTIVE);

        userRepository.save(user);

        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(Long id) {

        Customer customer = findById(id);

        User user = customer.getUser();

        customerRepository.delete(customer);

        if (user != null) {
            userRepository.delete(user);
        }
    }
}