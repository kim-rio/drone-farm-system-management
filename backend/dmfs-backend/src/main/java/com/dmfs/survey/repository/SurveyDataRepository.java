package com.dmfs.survey.repository;

import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.survey.entity.Survey;
import com.dmfs.survey.entity.SurveyData;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyDataRepository
        extends JpaRepository<SurveyData, Long> {

    /*
     * Get all uploaded data files belonging
     * to a particular survey.
     */
    List<SurveyData> findBySurveyOrderByUploadedAtDesc(
            Survey survey
    );


    /*
     * Get one survey-data record while enforcing
     * company-level tenant isolation.
     */
    Optional<SurveyData> findByIdAndCompany(
            Long id,
            SubscriberCompany company
    );


    /*
     * Get all uploaded survey data belonging
     * to the authenticated subscriber company.
     */
    List<SurveyData> findByCompanyOrderByUploadedAtDesc(
            SubscriberCompany company
    );


    /*
     * Get survey data for a particular survey
     * while enforcing tenant isolation.
     */
    List<SurveyData> findBySurveyAndCompanyOrderByUploadedAtDesc(
            Survey survey,
            SubscriberCompany company
    );


    /*
     * Find data uploaded by a specific user.
     */
    List<SurveyData> findByCompanyAndUploadedByIdOrderByUploadedAtDesc(
            SubscriberCompany company,
            Long uploadedById
    );
}