package com.dmfs.geologist.repository;

import com.dmfs.geologist.entity.MagneticAnomalyMap;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface MagneticAnomalyMapRepository extends JpaRepository<MagneticAnomalyMap, Long> {
    @Query("select m from MagneticAnomalyMap m join fetch m.survey s join fetch s.company order by m.generatedAt desc")
    List<MagneticAnomalyMap> findAllWithSurvey();
}
