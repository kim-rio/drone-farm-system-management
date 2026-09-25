package com.dmfs.survey.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ProcessingResultResponse {

    private boolean success;
    private String message;

    @JsonProperty("input_file")
    private String inputFile;

    @JsonProperty("output_file")
    private String outputFile;

    @JsonProperty("grid_file")
    private String gridFile;

    @JsonProperty("feature_file")
    private String featureFile;

    @JsonProperty("geotiff_file")
    private String geotiffFile;

    @JsonProperty("preview_file")
    private String previewFile;

    @JsonProperty("anomaly_geojson")
    private String anomalyGeojson;

    @JsonProperty("report_file")
    private String reportFile;

    @JsonProperty("record_count")
    private Integer recordCount;

    @JsonProperty("valid_record_count")
    private Integer validRecordCount;

    @JsonProperty("removed_record_count")
    private Integer removedRecordCount;

    @JsonProperty("latitude_column")
    private String latitudeColumn;

    @JsonProperty("longitude_column")
    private String longitudeColumn;

    @JsonProperty("magnetic_column")
    private String magneticColumn;

    @JsonProperty("igrf_declination_deg")
    private Double igrfDeclinationDeg;

    @JsonProperty("igrf_inclination_deg")
    private Double igrfInclinationDeg;

    @JsonProperty("igrf_total_intensity_nt")
    private Double igrfTotalIntensityNt;

    @JsonProperty("anomaly_count")
    private Integer anomalyCount;

    private List<AnomalyCandidateResponse> candidates =
            new ArrayList<>();

    private List<String> warnings =
            new ArrayList<>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getGridFile() {
        return gridFile;
    }

    public void setGridFile(String gridFile) {
        this.gridFile = gridFile;
    }

    public String getFeatureFile() {
        return featureFile;
    }

    public void setFeatureFile(String featureFile) {
        this.featureFile = featureFile;
    }

    public String getGeotiffFile() {
        return geotiffFile;
    }

    public void setGeotiffFile(String geotiffFile) {
        this.geotiffFile = geotiffFile;
    }

    public String getPreviewFile() {
        return previewFile;
    }

    public void setPreviewFile(String previewFile) {
        this.previewFile = previewFile;
    }

    public String getAnomalyGeojson() {
        return anomalyGeojson;
    }

    public void setAnomalyGeojson(String anomalyGeojson) {
        this.anomalyGeojson = anomalyGeojson;
    }

    public String getReportFile() {
        return reportFile;
    }

    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public Integer getValidRecordCount() {
        return validRecordCount;
    }

    public void setValidRecordCount(Integer validRecordCount) {
        this.validRecordCount = validRecordCount;
    }

    public Integer getRemovedRecordCount() {
        return removedRecordCount;
    }

    public void setRemovedRecordCount(Integer removedRecordCount) {
        this.removedRecordCount = removedRecordCount;
    }

    public String getLatitudeColumn() {
        return latitudeColumn;
    }

    public void setLatitudeColumn(String latitudeColumn) {
        this.latitudeColumn = latitudeColumn;
    }

    public String getLongitudeColumn() {
        return longitudeColumn;
    }

    public void setLongitudeColumn(String longitudeColumn) {
        this.longitudeColumn = longitudeColumn;
    }

    public String getMagneticColumn() {
        return magneticColumn;
    }

    public void setMagneticColumn(String magneticColumn) {
        this.magneticColumn = magneticColumn;
    }

    public Double getIgrfDeclinationDeg() {
        return igrfDeclinationDeg;
    }

    public void setIgrfDeclinationDeg(Double igrfDeclinationDeg) {
        this.igrfDeclinationDeg = igrfDeclinationDeg;
    }

    public Double getIgrfInclinationDeg() {
        return igrfInclinationDeg;
    }

    public void setIgrfInclinationDeg(Double igrfInclinationDeg) {
        this.igrfInclinationDeg = igrfInclinationDeg;
    }

    public Double getIgrfTotalIntensityNt() {
        return igrfTotalIntensityNt;
    }

    public void setIgrfTotalIntensityNt(Double igrfTotalIntensityNt) {
        this.igrfTotalIntensityNt = igrfTotalIntensityNt;
    }

    public Integer getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(Integer anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    public List<AnomalyCandidateResponse> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<AnomalyCandidateResponse> candidates) {
        this.candidates = candidates;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}