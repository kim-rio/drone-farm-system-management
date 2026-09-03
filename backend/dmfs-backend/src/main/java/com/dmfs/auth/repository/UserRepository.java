package com.dmfs.auth.repository;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.company.entity.SubscriberCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByCompanyAndRoleInOrderByFirstNameAsc(
            SubscriberCompany company,
            List<Role> roles
    );

    Optional<User> findByIdAndCompany(
            Long id,
            SubscriberCompany company
    );

    long countByActiveTrue();

    long countByActiveFalse();
}
