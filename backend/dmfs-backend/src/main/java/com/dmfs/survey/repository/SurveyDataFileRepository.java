package com.dmfs.survey.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmfs.survey.entity.SurveyDataFile;

public interface SurveyDataFileRepository
        extends JpaRepository<SurveyDataFile, Long> {

    List<SurveyDataFile> findByPackageEntityId(Long packageId);

    boolean existsByPackageEntityIdAndFileType(
            Long packageId,
            String fileType
    );
}