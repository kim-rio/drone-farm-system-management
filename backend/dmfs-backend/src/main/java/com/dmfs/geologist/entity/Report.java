package com.dmfs.geologist.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "report")
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "survey_id", nullable = false) private Survey survey;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ai_analysis_id", nullable = false) private AiAnalysis analysis;
    @Column(name = "report_name", nullable = false) private String reportName;
    @Column(name = "file_path", nullable = false) private String filePath;
    @Column(name = "file_type") private String fileType;
    @Column(name = "file_size") private Long fileSize;
    @Column(name = "generated_at") private OffsetDateTime generatedAt;
    public Long getId() { return id; } public Survey getSurvey() { return survey; } public void setSurvey(Survey v) { survey=v; }
    public AiAnalysis getAnalysis() { return analysis; } public void setAnalysis(AiAnalysis v) { analysis=v; }
    public String getReportName() { return reportName; } public void setReportName(String v) { reportName=v; }
    public String getFilePath() { return filePath; } public void setFilePath(String v) { filePath=v; }
    public String getFileType() { return fileType; } public void setFileType(String v) { fileType=v; }
    public Long getFileSize() { return fileSize; } public void setFileSize(Long v) { fileSize=v; }
    public OffsetDateTime getGeneratedAt() { return generatedAt; } public void setGeneratedAt(OffsetDateTime v) { generatedAt=v; }
}
