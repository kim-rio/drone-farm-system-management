package com.dmfs.client.entity;

import java.io.Serializable;
import java.util.Objects;

public class ClientUserId implements Serializable {

    private Long clientId;
    private Long userId;

    public ClientUserId() {
    }

    public ClientUserId(Long clientId, Long userId) {
        this.clientId = clientId;
        this.userId = userId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ClientUserId that)) {
            return false;
        }

        return Objects.equals(clientId, that.clientId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, userId);
    }
}
