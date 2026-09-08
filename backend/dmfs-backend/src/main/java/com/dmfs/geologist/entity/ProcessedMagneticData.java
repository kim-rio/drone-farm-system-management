package com.dmfs.geologist.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "processed_magnetic_data")
public class ProcessedMagneticData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "survey_id", nullable = false) private Survey survey;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "survey_data_id", nullable = false) private SurveyData surveyData;
    @Column(name = "file_name", nullable = false) private String fileName;
    @Column(name = "file_path", nullable = false) private String filePath;
    @Column(name = "file_type") private String fileType;
    @Column(name = "file_size") private Long fileSize;
    @Column(name = "record_count") private Long recordCount;
    @Column(name = "processed_at") private OffsetDateTime processedAt;
    public Long getId(){return id;} public Survey getSurvey(){return survey;} public void setSurvey(Survey v){survey=v;} public SurveyData getSurveyData(){return surveyData;} public void setSurveyData(SurveyData v){surveyData=v;}
    public String getFileName(){return fileName;} public void setFileName(String v){fileName=v;} public String getFilePath(){return filePath;} public void setFilePath(String v){filePath=v;}
    public String getFileType(){return fileType;} public void setFileType(String v){fileType=v;} public Long getFileSize(){return fileSize;} public void setFileSize(Long v){fileSize=v;}
    public Long getRecordCount(){return recordCount;} public void setRecordCount(Long v){recordCount=v;} public OffsetDateTime getProcessedAt(){return processedAt;} public void setProcessedAt(OffsetDateTime v){processedAt=v;}
}
