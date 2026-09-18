package com.dmfs.survey.dto;

public class SurveyDataFileResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private String fileExtension;
    private Long fileSize;
    private String validationStatus;
    private String validationMessage;

    public SurveyDataFileResponse() {
    }

    public SurveyDataFileResponse(
            Long id,
            String fileName,
            String fileType,
            String fileExtension,
            Long fileSize,
            String validationStatus,
            String validationMessage
    ) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.validationStatus = validationStatus;
        this.validationMessage = validationMessage;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}