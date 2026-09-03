package com.dmfs.superadmin.service;

import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.entity.CompanyStatus;
import com.dmfs.company.repository.SubscriberCompanyRepository;
import com.dmfs.superadmin.dto.SuperAdminDashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuperAdminDashboardService {

    private final SubscriberCompanyRepository companyRepository;
    private final UserRepository userRepository;

    public SuperAdminDashboardService(
            SubscriberCompanyRepository companyRepository,
            UserRepository userRepository
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public SuperAdminDashboardResponse getDashboard() {

        long totalCompanies =
                companyRepository.count();

        long activeCompanies =
                companyRepository.countByStatus(CompanyStatus.ACTIVE);

        long suspendedCompanies =
                companyRepository.countByStatus(CompanyStatus.SUSPENDED);

        long expiredCompanies =
                companyRepository.countByStatus(CompanyStatus.EXPIRED);

        long totalUsers =
                userRepository.count();

        long activeUsers =
                userRepository.countByActiveTrue();

        long inactiveUsers =
                userRepository.countByActiveFalse();

        return new SuperAdminDashboardResponse(
                totalCompanies,
                activeCompanies,
                suspendedCompanies,
                expiredCompanies,
                totalUsers,
                activeUsers,
                inactiveUsers
        );
    }
}