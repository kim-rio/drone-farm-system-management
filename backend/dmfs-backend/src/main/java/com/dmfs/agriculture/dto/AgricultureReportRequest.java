package com.dmfs.agriculture.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AgricultureReportRequest {
    public LocalDate applicationDate;
    public String operatorLicense, cropType, growthStage, targetProblem;
    public BigDecimal totalAreaTreatedHa, temperatureC, windSpeedKmh, relativeHumidity;
    public String applicationStartTime, applicationEndTime, windDirection, skyConditions, rainForecast;
    public String tradeName, registrationNumber, activeIngredient, adjuvants;
    public BigDecimal totalProductUsedL, totalWaterVolumeL, productRateLHa, waterRateLHa;
    public String equipmentUsed, nozzleType;
    public BigDecimal dropletSizeMicrons;
    public Integer preHarvestIntervalDays, restrictedEntryIntervalHours;
    public String bufferZoneNotes, coverageQuality, coverageObservations, incidentsNotes;
    public LocalDate nextScoutDate;
    public String applicatorSignature;
    public Boolean finalizeReport;
}
