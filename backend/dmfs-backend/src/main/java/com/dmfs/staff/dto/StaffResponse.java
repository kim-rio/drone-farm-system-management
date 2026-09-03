package com.dmfs.staff.dto;

import java.time.LocalDateTime;

public class StaffResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StaffResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String role,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
