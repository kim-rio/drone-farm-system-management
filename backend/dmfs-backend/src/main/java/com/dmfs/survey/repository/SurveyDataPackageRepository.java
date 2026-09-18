package com.dmfs.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmfs.survey.entity.SurveyDataPackage;

public interface SurveyDataPackageRepository
        extends JpaRepository<SurveyDataPackage, Long> {

    Optional<SurveyDataPackage> findByPackageCode(String packageCode);

    List<SurveyDataPackage> findBySurveyId(Long surveyId);

    boolean existsByPackageCode(String packageCode);
}