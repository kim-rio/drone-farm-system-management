package com.dmfs.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.survey.entity.Surveyy;

public interface SurveyyRepository
        extends JpaRepository<Surveyy, Long> {

    /*
     * Get all surveys belonging to the
     * authenticated subscriber company.
     */
    List<Surveyy> findByCompanyOrderByCreatedAtDesc(
            SubscriberCompany company
    );


    /*
     * Find one survey while enforcing
     * company-level tenant isolation.
     */
    Optional<Surveyy> findByIdAndCompany(
            Long id,
            SubscriberCompany company
    );


    /*
     * Prevent duplicate surveys for the
     * same service request.
     */
    boolean existsByServiceRequest(
            ServiceRequest serviceRequest
    );


    /*
     * Find surveys associated with a
     * particular service request.
     */
    List<Surveyy> findByServiceRequestOrderByCreatedAtDesc(
            ServiceRequest serviceRequest
    );


    /*
     * Find surveys assigned to a
     * particular operator.
     */
    List<Surveyy> findByCompanyAndOperatorIdOrderByCreatedAtDesc(
            SubscriberCompany company,
            Long operatorId
    );


    /*
     * Check whether a survey code already
     * exists inside a particular company.
     */
    boolean existsByCompanyAndSurveyCode(
            SubscriberCompany company,
            String surveyCode
    );
}