package com.dmfs.client.repository;

import com.dmfs.client.entity.ClientInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientInvitationRepository
        extends JpaRepository<ClientInvitation, Long> {

    Optional<ClientInvitation> findByTokenHash(String tokenHash);

    List<ClientInvitation> findByClientIdAndUsedAtIsNull(Long clientId);
}
