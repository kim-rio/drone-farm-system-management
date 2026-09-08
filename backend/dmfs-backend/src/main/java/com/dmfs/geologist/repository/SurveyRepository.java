package com.dmfs.geologist.repository;

import com.dmfs.geologist.entity.Survey;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.OffsetDateTime;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    @Query("select s from Survey s join fetch s.company order by s.startedAt desc")
    List<Survey> findAllWithCompany();
    long countByStatusIgnoreCase(String status);
    List<Survey> findByOperatorIdOrderByStartedAtDesc(Long operatorId);
    @Modifying
    @Query(value = "update survey set equipment_used = :equipment, completed_at = :completedAt, ended_at = :completedAt, completed_bounds = ST_MakeEnvelope(:minLongitude, :minLatitude, :maxLongitude, :maxLatitude, 4326) where id = :surveyId", nativeQuery = true)
    void completeWithBounds(@Param("surveyId") Long surveyId, @Param("equipment") String equipment, @Param("completedAt") OffsetDateTime completedAt, @Param("minLatitude") double minLatitude, @Param("minLongitude") double minLongitude, @Param("maxLatitude") double maxLatitude, @Param("maxLongitude") double maxLongitude);
}
