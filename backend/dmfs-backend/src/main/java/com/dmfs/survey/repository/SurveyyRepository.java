package com.dmfs.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.survey.entity.Surveyy;
import com.dmfs.survey.entity.SurveyStatus;

public interface SurveyyRepository
        extends JpaRepository<Surveyy, Long> {

    /*
     * ============================================================
     * Company / tenant queries
     * ============================================================
     */

    List<Surveyy> findByCompanyOrderByCreatedAtDesc(
            SubscriberCompany company
    );

    Optional<Surveyy> findByIdAndCompany(
            Long id,
            SubscriberCompany company
    );

    /*
     * ============================================================
     * Service request queries
     * ============================================================
     */

    boolean existsByServiceRequest(
            ServiceRequest serviceRequest
    );

    List<Surveyy> findByServiceRequestOrderByCreatedAtDesc(
            ServiceRequest serviceRequest
    );

    /*
     * ============================================================
     * Operator queries
     * ============================================================
     */

    List<Surveyy> findByCompanyAndOperatorIdOrderByCreatedAtDesc(
            SubscriberCompany company,
            Long operatorId
    );

    /*
     * ============================================================
     * Survey code
     * ============================================================
     */

    boolean existsByCompanyAndSurveyCode(
            SubscriberCompany company,
            String surveyCode
    );

    /*
     * ============================================================
     * Geologist dashboard
     * ============================================================
     */

    long countByStatus(
            SurveyStatus status
    );

    List<Surveyy> findByStatusOrderByCreatedAtDesc(
            SurveyStatus status
    );
}
