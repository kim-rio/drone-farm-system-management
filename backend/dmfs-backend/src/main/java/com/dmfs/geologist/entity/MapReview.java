package com.dmfs.geologist.entity;

import com.dmfs.auth.entity.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "map_review")
public class MapReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "magnetic_anomaly_map_id", nullable = false) private MagneticAnomalyMap map;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "geologist_id", nullable = false) private User geologist;
    @Column(nullable = false) private String decision = "PENDING";
    @Column(columnDefinition = "TEXT") private String comment;
    @Column(name = "reviewed_at") private OffsetDateTime reviewedAt;
    public Long getId() { return id; }
    public MagneticAnomalyMap getMap() { return map; } public void setMap(MagneticAnomalyMap v) { map = v; }
    public User getGeologist() { return geologist; } public void setGeologist(User v) { geologist = v; }
    public String getDecision() { return decision; } public void setDecision(String v) { decision = v; }
    public String getComment() { return comment; } public void setComment(String v) { comment = v; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; } public void setReviewedAt(OffsetDateTime v) { reviewedAt = v; }
}
