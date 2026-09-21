package com.dmfs.client.entity;

import com.dmfs.auth.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "client_users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_client_users_user",
            columnNames = "user_id"
        )
    }
)
@IdClass(ClientUserId.class)
public class ClientUser {

    @Id
    @Column(name = "client_id")
    private Long clientId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "client_id",
        insertable = false,
        updatable = false
    )
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        insertable = false,
        updatable = false
    )
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Client getClient() {
        return client;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
