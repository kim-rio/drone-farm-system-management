package com.dmfs.geologist.repository;

import com.dmfs.geologist.entity.ProcessedMagneticData;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessedMagneticDataRepository
        extends JpaRepository<ProcessedMagneticData, Long> {

    List<ProcessedMagneticData>
    findBySurvey_IdOrderByProcessedAtDesc(
            Long surveyId
    );

    Optional<ProcessedMagneticData>
    findFirstBySurvey_IdOrderByProcessedAtDesc(
            Long surveyId
    );
}