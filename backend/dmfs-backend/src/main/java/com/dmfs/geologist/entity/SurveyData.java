package com.dmfs.geologist.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "survey_data")
public class SurveyData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "survey_id", nullable = false) private Survey survey;
    @Column(name = "company_id", nullable = false) private Long companyId;
    @Column(name = "file_name", nullable = false) private String fileName;
    @Column(name = "file_path", nullable = false) private String filePath;
    @Column(name = "file_type") private String fileType;
    @Column(name = "file_size") private Long fileSize;
    @Column(name = "record_count") private Long recordCount;
    @Column(name = "uploaded_by", nullable = false) private Long uploadedBy;
    @Column(name = "uploaded_at") private OffsetDateTime uploadedAt;
    public Long getId(){return id;} public Survey getSurvey(){return survey;} public void setSurvey(Survey v){survey=v;}
    public Long getCompanyId(){return companyId;} public void setCompanyId(Long v){companyId=v;} public String getFileName(){return fileName;} public void setFileName(String v){fileName=v;}
    public String getFilePath(){return filePath;} public void setFilePath(String v){filePath=v;} public String getFileType(){return fileType;} public void setFileType(String v){fileType=v;}
    public Long getFileSize(){return fileSize;} public void setFileSize(Long v){fileSize=v;} public Long getRecordCount(){return recordCount;} public void setRecordCount(Long v){recordCount=v;}
    public Long getUploadedBy(){return uploadedBy;} public void setUploadedBy(Long v){uploadedBy=v;} public OffsetDateTime getUploadedAt(){return uploadedAt;} public void setUploadedAt(OffsetDateTime v){uploadedAt=v;}
}
