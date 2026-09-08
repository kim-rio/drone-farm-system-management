package com.dmfs.geologist.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "magnetic_anomaly_map")
public class MagneticAnomalyMap {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "survey_id", nullable = false) private Survey survey;
    @Column(name = "processed_magnetic_data_id", nullable = false) private Long processedMagneticDataId;
    @Column(name = "map_name", nullable = false) private String mapName;
    @Column(name = "file_path", nullable = false) private String filePath;
    @Column(name = "file_type") private String fileType;
    @Column(name = "file_size") private Long fileSize;
    @Column(name = "anomaly_count", nullable = false) private Integer anomalyCount = 0;
    @Column(name = "generated_at") private OffsetDateTime generatedAt;
    public Long getId() { return id; }
    public Survey getSurvey() { return survey; } public void setSurvey(Survey v) { survey = v; }
    public Long getProcessedMagneticDataId() { return processedMagneticDataId; } public void setProcessedMagneticDataId(Long v) { processedMagneticDataId = v; }
    public String getMapName() { return mapName; } public void setMapName(String v) { mapName = v; }
    public String getFilePath() { return filePath; } public void setFilePath(String v) { filePath = v; }
    public String getFileType() { return fileType; } public void setFileType(String v) { fileType = v; }
    public Long getFileSize() { return fileSize; } public void setFileSize(Long v) { fileSize = v; }
    public Integer getAnomalyCount() { return anomalyCount; } public void setAnomalyCount(Integer v) { anomalyCount = v; }
    public OffsetDateTime getGeneratedAt() { return generatedAt; } public void setGeneratedAt(OffsetDateTime v) { generatedAt = v; }
}
