package com.dmfs.farm.repository;

import com.dmfs.customer.entity.Customer;
import com.dmfs.farm.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {

    List<Farm> findByCustomer(Customer customer);

    List<Farm> findByCustomerId(Long customerId);
}