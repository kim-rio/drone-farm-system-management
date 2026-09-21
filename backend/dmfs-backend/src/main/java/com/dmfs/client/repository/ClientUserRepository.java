package com.dmfs.client.repository;

import com.dmfs.client.entity.ClientUser;
import com.dmfs.client.entity.ClientUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientUserRepository
        extends JpaRepository<ClientUser, ClientUserId> {

    Optional<ClientUser> findByUserId(Long userId);

    Optional<ClientUser> findByClientId(Long clientId);

    boolean existsByUserId(Long userId);

    boolean existsByClientId(Long clientId);
}
