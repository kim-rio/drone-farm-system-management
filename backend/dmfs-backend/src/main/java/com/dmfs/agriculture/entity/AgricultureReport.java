package com.dmfs.agriculture.entity;

import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.mission.entity.Mission;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agriculture_reports",
       uniqueConstraints = @UniqueConstraint(name = "uk_agriculture_report_mission", columnNames = "mission_id"))
public class AgricultureReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="company_id", nullable=false) private SubscriberCompany company;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="mission_id", nullable=false, unique=true) private Mission mission;

    @Column(name="report_code", nullable=false, unique=true, length=40) private String reportCode;
    @Column(name="application_date", nullable=false) private LocalDate applicationDate;
    @Column(name="operator_license", length=80) private String operatorLicense;
    @Column(name="crop_type", nullable=false, length=150) private String cropType;
    @Column(name="growth_stage", length=100) private String growthStage;
    @Column(name="total_area_treated_ha", precision=12, scale=2) private BigDecimal totalAreaTreatedHa;
    @Column(name="target_problem", columnDefinition="TEXT") private String targetProblem;
    @Column(name="application_start_time", length=20) private String applicationStartTime;
    @Column(name="application_end_time", length=20) private String applicationEndTime;
    @Column(name="temperature_c", precision=6, scale=2) private BigDecimal temperatureC;
    @Column(name="wind_speed_kmh", precision=6, scale=2) private BigDecimal windSpeedKmh;
    @Column(name="wind_direction", length=40) private String windDirection;
    @Column(name="relative_humidity", precision=6, scale=2) private BigDecimal relativeHumidity;
    @Column(name="sky_conditions", length=120) private String skyConditions;
    @Column(name="rain_forecast", length=200) private String rainForecast;
    @Column(name="trade_name", length=150) private String tradeName;
    @Column(name="registration_number", length=100) private String registrationNumber;
    @Column(name="active_ingredient", length=200) private String activeIngredient;
    @Column(name="total_product_used_l", precision=12, scale=2) private BigDecimal totalProductUsedL;
    @Column(name="total_water_volume_l", precision=12, scale=2) private BigDecimal totalWaterVolumeL;
    @Column(name="product_rate_l_ha", precision=12, scale=4) private BigDecimal productRateLHa;
    @Column(name="water_rate_l_ha", precision=12, scale=4) private BigDecimal waterRateLHa;
    @Column(name="adjuvants", columnDefinition="TEXT") private String adjuvants;
    @Column(name="equipment_used", length=200) private String equipmentUsed;
    @Column(name="nozzle_type", length=200) private String nozzleType;
    @Column(name="droplet_size_microns", precision=8, scale=2) private BigDecimal dropletSizeMicrons;
    @Column(name="pre_harvest_interval_days") private Integer preHarvestIntervalDays;
    @Column(name="restricted_entry_interval_hours") private Integer restrictedEntryIntervalHours;
    @Column(name="buffer_zone_notes", columnDefinition="TEXT") private String bufferZoneNotes;
    @Column(name="coverage_quality", length=100) private String coverageQuality;
    @Column(name="coverage_observations", columnDefinition="TEXT") private String coverageObservations;
    @Column(name="incidents_notes", columnDefinition="TEXT") private String incidentsNotes;
    @Column(name="next_scout_date") private LocalDate nextScoutDate;
    @Column(name="applicator_signature", length=150) private String applicatorSignature;
    @Column(name="photo_1_path", length=500) private String photo1Path;
    @Column(name="photo_2_path", length=500) private String photo2Path;
    @Column(name="finalized", nullable=false) private boolean finalized = false;
    @Column(name="created_at", nullable=false, updatable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt=LocalDateTime.now(); updatedAt=createdAt; }
    @PreUpdate void onUpdate(){ updatedAt=LocalDateTime.now(); }

    public Long getId(){return id;} public SubscriberCompany getCompany(){return company;} public void setCompany(SubscriberCompany v){company=v;}
    public Mission getMission(){return mission;} public void setMission(Mission v){mission=v;}
    public String getReportCode(){return reportCode;} public void setReportCode(String v){reportCode=v;}
    public LocalDate getApplicationDate(){return applicationDate;} public void setApplicationDate(LocalDate v){applicationDate=v;}
    public String getOperatorLicense(){return operatorLicense;} public void setOperatorLicense(String v){operatorLicense=v;}
    public String getCropType(){return cropType;} public void setCropType(String v){cropType=v;}
    public String getGrowthStage(){return growthStage;} public void setGrowthStage(String v){growthStage=v;}
    public BigDecimal getTotalAreaTreatedHa(){return totalAreaTreatedHa;} public void setTotalAreaTreatedHa(BigDecimal v){totalAreaTreatedHa=v;}
    public String getTargetProblem(){return targetProblem;} public void setTargetProblem(String v){targetProblem=v;}
    public String getApplicationStartTime(){return applicationStartTime;} public void setApplicationStartTime(String v){applicationStartTime=v;}
    public String getApplicationEndTime(){return applicationEndTime;} public void setApplicationEndTime(String v){applicationEndTime=v;}
    public BigDecimal getTemperatureC(){return temperatureC;} public void setTemperatureC(BigDecimal v){temperatureC=v;}
    public BigDecimal getWindSpeedKmh(){return windSpeedKmh;} public void setWindSpeedKmh(BigDecimal v){windSpeedKmh=v;}
    public String getWindDirection(){return windDirection;} public void setWindDirection(String v){windDirection=v;}
    public BigDecimal getRelativeHumidity(){return relativeHumidity;} public void setRelativeHumidity(BigDecimal v){relativeHumidity=v;}
    public String getSkyConditions(){return skyConditions;} public void setSkyConditions(String v){skyConditions=v;}
    public String getRainForecast(){return rainForecast;} public void setRainForecast(String v){rainForecast=v;}
    public String getTradeName(){return tradeName;} public void setTradeName(String v){tradeName=v;}
    public String getRegistrationNumber(){return registrationNumber;} public void setRegistrationNumber(String v){registrationNumber=v;}
    public String getActiveIngredient(){return activeIngredient;} public void setActiveIngredient(String v){activeIngredient=v;}
    public BigDecimal getTotalProductUsedL(){return totalProductUsedL;} public void setTotalProductUsedL(BigDecimal v){totalProductUsedL=v;}
    public BigDecimal getTotalWaterVolumeL(){return totalWaterVolumeL;} public void setTotalWaterVolumeL(BigDecimal v){totalWaterVolumeL=v;}
    public BigDecimal getProductRateLHa(){return productRateLHa;} public void setProductRateLHa(BigDecimal v){productRateLHa=v;}
    public BigDecimal getWaterRateLHa(){return waterRateLHa;} public void setWaterRateLHa(BigDecimal v){waterRateLHa=v;}
    public String getAdjuvants(){return adjuvants;} public void setAdjuvants(String v){adjuvants=v;}
    public String getEquipmentUsed(){return equipmentUsed;} public void setEquipmentUsed(String v){equipmentUsed=v;}
    public String getNozzleType(){return nozzleType;} public void setNozzleType(String v){nozzleType=v;}
    public BigDecimal getDropletSizeMicrons(){return dropletSizeMicrons;} public void setDropletSizeMicrons(BigDecimal v){dropletSizeMicrons=v;}
    public Integer getPreHarvestIntervalDays(){return preHarvestIntervalDays;} public void setPreHarvestIntervalDays(Integer v){preHarvestIntervalDays=v;}
    public Integer getRestrictedEntryIntervalHours(){return restrictedEntryIntervalHours;} public void setRestrictedEntryIntervalHours(Integer v){restrictedEntryIntervalHours=v;}
    public String getBufferZoneNotes(){return bufferZoneNotes;} public void setBufferZoneNotes(String v){bufferZoneNotes=v;}
    public String getCoverageQuality(){return coverageQuality;} public void setCoverageQuality(String v){coverageQuality=v;}
    public String getCoverageObservations(){return coverageObservations;} public void setCoverageObservations(String v){coverageObservations=v;}
    public String getIncidentsNotes(){return incidentsNotes;} public void setIncidentsNotes(String v){incidentsNotes=v;}
    public LocalDate getNextScoutDate(){return nextScoutDate;} public void setNextScoutDate(LocalDate v){nextScoutDate=v;}
    public String getApplicatorSignature(){return applicatorSignature;} public void setApplicatorSignature(String v){applicatorSignature=v;}
    public String getPhoto1Path(){return photo1Path;} public void setPhoto1Path(String v){photo1Path=v;}
    public String getPhoto2Path(){return photo2Path;} public void setPhoto2Path(String v){photo2Path=v;}
    public boolean isFinalized(){return finalized;} public void setFinalized(boolean v){finalized=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
