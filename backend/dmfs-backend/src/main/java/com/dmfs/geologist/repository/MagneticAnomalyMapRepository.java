package com.dmfs.geologist.repository;

import com.dmfs.geologist.entity.MagneticAnomalyMap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MagneticAnomalyMapRepository
        extends JpaRepository<MagneticAnomalyMap, Long> {

    @Query("""
        SELECT m
        FROM MagneticAnomalyMap m
        ORDER BY m.generatedAt DESC
    """)
    List<MagneticAnomalyMap> findAllWithSurvey();

    /*
     * Find all anomaly maps generated from a specific survey.
     */
    List<MagneticAnomalyMap> findBySurvey_IdOrderByGeneratedAtDesc(
            Long surveyId
    );
}