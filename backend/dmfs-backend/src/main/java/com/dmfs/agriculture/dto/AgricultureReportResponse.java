package com.dmfs.agriculture.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AgricultureReportResponse {
    public Long id, missionId, farmId, farmBlockId, operatorId;
    public String reportCode, missionCode, category, farmName, farmBlockName, operatorName, operatorLicense;
    public LocalDate applicationDate, nextScoutDate;
    public BigDecimal farmAreaHa, blockAreaHa, totalAreaTreatedHa, temperatureC, windSpeedKmh, relativeHumidity,
            totalProductUsedL, totalWaterVolumeL, productRateLHa, waterRateLHa, dropletSizeMicrons;
    public String cropType, growthStage, targetProblem, applicationStartTime, applicationEndTime, windDirection,
            skyConditions, rainForecast, tradeName, registrationNumber, activeIngredient, adjuvants,
            equipmentUsed, nozzleType, bufferZoneNotes, coverageQuality, coverageObservations, incidentsNotes,
            applicatorSignature, photo1Url, photo2Url;
    public Integer preHarvestIntervalDays, restrictedEntryIntervalHours;
    public boolean finalized;
    public LocalDateTime createdAt, updatedAt;
}
