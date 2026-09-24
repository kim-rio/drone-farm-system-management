package com.dmfs.agriculture.repository;

import com.dmfs.agriculture.entity.AgricultureReport;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AgricultureReportRepository extends JpaRepository<AgricultureReport, Long> {
    Optional<AgricultureReport> findByMission(Mission mission);
    Optional<AgricultureReport> findByIdAndCompany(Long id, SubscriberCompany company);
    List<AgricultureReport> findByCompanyOrderByCreatedAtDesc(SubscriberCompany company);
    long countByCompany(SubscriberCompany company);
}
