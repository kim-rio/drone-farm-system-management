package com.dmfs.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.survey.entity.Surveyy;
import com.dmfs.survey.entity.SurveyyData;

public interface SurveyyDataRepository
        extends JpaRepository<SurveyyData, Long> {

    /*
     * Get all uploaded data files belonging
     * to a particular survey.
     */
    List<SurveyyData> findBySurveyOrderByUploadedAtDesc(
            Surveyy survey
    );


    /*
     * Get one survey-data record while enforcing
     * company-level tenant isolation.
     */
    Optional<SurveyyData> findByIdAndCompany(
            Long id,
            SubscriberCompany company
    );


    /*
     * Get all uploaded survey data belonging
     * to the authenticated subscriber company.
     */
    List<SurveyyData> findByCompanyOrderByUploadedAtDesc(
            SubscriberCompany company
    );


    /*
     * Get survey data for a particular survey
     * while enforcing tenant isolation.
     */
    List<SurveyyData> findBySurveyAndCompanyOrderByUploadedAtDesc(
            Surveyy survey,
            SubscriberCompany company
    );


    /*
     * Find data uploaded by a specific user.
     */
    List<SurveyyData> findByCompanyAndUploadedByIdOrderByUploadedAtDesc(
            SubscriberCompany company,
            Long uploadedById
    );
}