package com.dmfs.farm.repository;

import com.dmfs.client.entity.Client;
import com.dmfs.farm.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {

    List<Farm> findByCustomer(Client customer);

    List<Farm> findByCustomerId(Long customerId);
}
