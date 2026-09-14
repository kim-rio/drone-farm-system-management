package com.dmfs.management.service;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.client.repository.ClientRepository;
import com.dmfs.farm.repository.BlockRepository;
import com.dmfs.farm.repository.FarmRepository;
import com.dmfs.management.dto.ManagementDashboardResponse;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagementDashboardService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final FarmRepository farmRepository;
    private final BlockRepository blockRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public ManagementDashboardService(
            UserRepository userRepository,
            ClientRepository clientRepository,
            FarmRepository farmRepository,
            BlockRepository blockRepository,
            ServiceRequestRepository serviceRequestRepository
    ) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.farmRepository = farmRepository;
        this.blockRepository = blockRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public ManagementDashboardResponse getDashboard(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found")
                );

        if (user.getCompany() == null) {
            throw new IllegalStateException(
                    "Management user is not assigned to a company"
            );
        }

        Long companyId = user.getCompany().getId();

        long clients =
                clientRepository.countByCompanyId(companyId);

        long farms =
                farmRepository.countByCustomerCompanyId(companyId);

        long blocks =
                blockRepository.countByFarmCustomerCompanyId(companyId);

        List<ServiceRequest> requests =
                serviceRequestRepository.findByCustomerCompanyId(companyId);

        Map<String, Long> statuses =
                new LinkedHashMap<>();

        for (ServiceRequest request : requests) {

            String status = request.getStatus();

            if (status == null || status.isBlank()) {
                status = "UNKNOWN";
            }

            status = status.trim().toUpperCase();

            statuses.merge(status, 1L, Long::sum);
        }

        return new ManagementDashboardResponse(
                clients,
                farms,
                blocks,
                requests.size(),
                statuses
        );
    }
}
