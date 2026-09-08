package com.dmfs.geologist.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "ai_analysis")
public class AiAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "survey_id", nullable = false) private Survey survey;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "magnetic_anomaly_map_id", nullable = false) private MagneticAnomalyMap map;
    @Column(name = "analysis_result", columnDefinition = "TEXT") private String analysisResult;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    public Long getId() { return id; } public Survey getSurvey() { return survey; } public void setSurvey(Survey v) { survey=v; }
    public MagneticAnomalyMap getMap() { return map; } public void setMap(MagneticAnomalyMap v) { map=v; }
    public String getAnalysisResult() { return analysisResult; } public void setAnalysisResult(String v) { analysisResult=v; }
    public OffsetDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(OffsetDateTime v) { createdAt=v; }
}
