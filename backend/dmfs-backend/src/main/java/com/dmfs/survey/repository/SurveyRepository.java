package com.dmfs.survey.repository;

import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.survey.entity.Survey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyRepository
        extends JpaRepository<Survey, Long> {

    /*
     * Get all surveys belonging to the
     * authenticated subscriber company.
     */
    List<Survey> findByCompanyOrderByCreatedAtDesc(
            SubscriberCompany company
    );


    /*
     * Find one survey while enforcing
     * company-level tenant isolation.
     */
    Optional<Survey> findByIdAndCompany(
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
    List<Survey> findByServiceRequestOrderByCreatedAtDesc(
            ServiceRequest serviceRequest
    );


    /*
     * Find surveys assigned to a
     * particular operator.
     */
    List<Survey> findByCompanyAndOperatorIdOrderByCreatedAtDesc(
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