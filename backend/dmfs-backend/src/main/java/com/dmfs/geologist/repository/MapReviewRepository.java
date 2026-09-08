package com.dmfs.geologist.repository;

import com.dmfs.geologist.entity.MapReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MapReviewRepository extends JpaRepository<MapReview, Long> {
    Optional<MapReview> findFirstByMapIdOrderByReviewedAtDesc(Long mapId);
    long countByDecisionIgnoreCase(String decision);
}
