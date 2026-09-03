package com.dmfs.superadmin.dto;

public class SuperAdminDashboardResponse {

    private long totalCompanies;
    private long activeCompanies;
    private long suspendedCompanies;
    private long expiredCompanies;

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;

    public SuperAdminDashboardResponse(
            long totalCompanies,
            long activeCompanies,
            long suspendedCompanies,
            long expiredCompanies,
            long totalUsers,
            long activeUsers,
            long inactiveUsers
    ) {
        this.totalCompanies = totalCompanies;
        this.activeCompanies = activeCompanies;
        this.suspendedCompanies = suspendedCompanies;
        this.expiredCompanies = expiredCompanies;
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
    }

    public long getTotalCompanies() {
        return totalCompanies;
    }

    public long getActiveCompanies() {
        return activeCompanies;
    }

    public long getSuspendedCompanies() {
        return suspendedCompanies;
    }

    public long getExpiredCompanies() {
        return expiredCompanies;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }
}