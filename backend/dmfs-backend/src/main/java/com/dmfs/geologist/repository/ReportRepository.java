package com.dmfs.geologist.repository;

import com.dmfs.geologist.entity.Report;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("select r from Report r join fetch r.survey order by r.generatedAt desc")
    List<Report> findAllWithSurvey();
}
