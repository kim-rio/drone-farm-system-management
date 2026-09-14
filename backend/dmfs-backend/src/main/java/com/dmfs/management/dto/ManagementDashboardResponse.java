package com.dmfs.management.dto;

import java.util.Map;

public class ManagementDashboardResponse {

    private long clients;
    private long farms;
    private long blocks;
    private long serviceRequests;
    private long pendingPayments;
    private long paidPayments;
    private Map<String, Long> requestStatuses;

    public ManagementDashboardResponse() {
    }

    public ManagementDashboardResponse(
            long clients,
            long farms,
            long blocks,
            long serviceRequests,
            Map<String, Long> requestStatuses
    ) {
        this.clients = clients;
        this.farms = farms;
        this.blocks = blocks;
        this.serviceRequests = serviceRequests;
        this.requestStatuses = requestStatuses;
    }

    public long getClients() {
        return clients;
    }

    public long getFarms() {
        return farms;
    }

    public long getBlocks() {
        return blocks;
    }

    public long getPendingPayments() { return pendingPayments; }
    public void setPendingPayments(long value) { this.pendingPayments = value; }

    public long getPaidPayments() { return paidPayments; }
    public void setPaidPayments(long value) { this.paidPayments = value; }

    public long getServiceRequests() {
        return serviceRequests;
    }

    public Map<String, Long> getRequestStatuses() {
        return requestStatuses;
    }
}
