package com.dmfs.survey.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnomalyCandidateResponse {

    private Long id;

    private Double easting;
    private Double northing;

    @JsonProperty("peak_residual_nT")
    private Double peakResidualNt;

    @JsonProperty("max_analytic_signal")
    private Double maxAnalyticSignal;

    @JsonProperty("area_m2")
    private Double areaM2;

    @JsonProperty("equivalent_radius_m")
    private Double equivalentRadiusM;

    @JsonProperty("estimated_depth_m")
    private Double estimatedDepthM;

    @JsonProperty("pixel_x")
    private Integer pixelX;

    @JsonProperty("pixel_y")
    private Integer pixelY;

    @JsonProperty("interpretation_status")
    private String interpretationStatus;

    @JsonProperty("requires_geologist_review")
    private Boolean requiresGeologistReview;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getEasting() {
        return easting;
    }

    public void setEasting(Double easting) {
        this.easting = easting;
    }

    public Double getNorthing() {
        return northing;
    }

    public void setNorthing(Double northing) {
        this.northing = northing;
    }

    public Double getPeakResidualNt() {
        return peakResidualNt;
    }

    public void setPeakResidualNt(Double peakResidualNt) {
        this.peakResidualNt = peakResidualNt;
    }

    public Double getMaxAnalyticSignal() {
        return maxAnalyticSignal;
    }

    public void setMaxAnalyticSignal(Double maxAnalyticSignal) {
        this.maxAnalyticSignal = maxAnalyticSignal;
    }

    public Double getAreaM2() {
        return areaM2;
    }

    public void setAreaM2(Double areaM2) {
        this.areaM2 = areaM2;
    }

    public Double getEquivalentRadiusM() {
        return equivalentRadiusM;
    }

    public void setEquivalentRadiusM(Double equivalentRadiusM) {
        this.equivalentRadiusM = equivalentRadiusM;
    }

    public Double getEstimatedDepthM() {
        return estimatedDepthM;
    }

    public void setEstimatedDepthM(Double estimatedDepthM) {
        this.estimatedDepthM = estimatedDepthM;
    }

    public Integer getPixelX() {
        return pixelX;
    }

    public void setPixelX(Integer pixelX) {
        this.pixelX = pixelX;
    }

    public Integer getPixelY() {
        return pixelY;
    }

    public void setPixelY(Integer pixelY) {
        this.pixelY = pixelY;
    }

    public String getInterpretationStatus() {
        return interpretationStatus;
    }

    public void setInterpretationStatus(String interpretationStatus) {
        this.interpretationStatus = interpretationStatus;
    }

    public Boolean getRequiresGeologistReview() {
        return requiresGeologistReview;
    }

    public void setRequiresGeologistReview(Boolean requiresGeologistReview) {
        this.requiresGeologistReview = requiresGeologistReview;
    }
}