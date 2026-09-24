package com.dmfs.agriculture.service;

import com.dmfs.agriculture.dto.AgricultureReportRequest;
import com.dmfs.agriculture.dto.AgricultureReportResponse;
import com.dmfs.agriculture.entity.AgricultureReport;
import com.dmfs.agriculture.repository.AgricultureReportRepository;
import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.mission.entity.Mission;
import com.dmfs.mission.repository.MissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AgricultureReportService {
    private final AgricultureReportRepository reportRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final Path storageRoot;

    public AgricultureReportService(
            AgricultureReportRepository reportRepository,
            MissionRepository missionRepository,
            UserRepository userRepository,
            @Value("${dmfs.storage.agriculture-reports:storage/agriculture-reports}") String storagePath) {
        this.reportRepository = reportRepository;
        this.missionRepository = missionRepository;
        this.userRepository = userRepository;
        this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<AgricultureReportResponse> list() {
        User user = currentOperatorOrThrow();
        return reportRepository.findByCompanyOrderByCreatedAtDesc(user.getCompany())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AgricultureReportResponse get(Long id) {
        User user = currentOperatorOrThrow();
        return reportRepository.findByIdAndCompany(id, user.getCompany())
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Agriculture report not found"));
    }

    @Transactional(readOnly = true)
    public AgricultureReportResponse getForMission(Long missionId) {
        User user = currentOperatorOrThrow();
        Mission mission = mission(missionId, user);
        if (!"AGRICULTURE".equals(mission.getCategory())) {
            throw new IllegalArgumentException("This mission is not an agriculture mission");
        }
        return reportRepository.findByMission(mission)
                .map(this::toResponse)
                .orElseGet(() -> draftResponse(mission));
    }

    @Transactional
    public AgricultureReportResponse save(
            Long missionId,
            AgricultureReportRequest request,
            MultipartFile photo1,
            MultipartFile photo2) throws IOException {

        User user = currentOperatorOrThrow();
        Mission mission = mission(missionId, user);

        if (!"AGRICULTURE".equals(mission.getCategory())) {
            throw new IllegalArgumentException("This mission is not an agriculture mission");
        }
        if (mission.getStatus().name().equals("CANCELLED")) {
            throw new IllegalArgumentException("Cancelled missions cannot have reports");
        }
        validate(request);

        AgricultureReport report = reportRepository.findByMission(mission).orElseGet(() -> {
            AgricultureReport r = new AgricultureReport();
            r.setCompany(user.getCompany());
            r.setMission(mission);
            r.setReportCode(generateCode());
            return r;
        });

        apply(report, request);

        if (photo1 != null && !photo1.isEmpty()) {
            report.setPhoto1Path(storePhoto(report, photo1, "photo-1"));
        }
        if (photo2 != null && !photo2.isEmpty()) {
            report.setPhoto2Path(storePhoto(report, photo2, "photo-2"));
        }

        boolean finalize = Boolean.TRUE.equals(request.finalizeReport);
        if (finalize) {
            if (isBlank(report.getPhoto1Path()) || isBlank(report.getPhoto2Path())) {
                throw new IllegalArgumentException("Two farm/application photos are required before finalizing the report");
            }
            report.setFinalized(true);
        }

        return toResponse(reportRepository.save(report));
    }

    public Resource photo(Long id, int slot) {
        User user = currentOperatorOrThrow();
        AgricultureReport report = reportRepository.findByIdAndCompany(id, user.getCompany())
                .orElseThrow(() -> new RuntimeException("Agriculture report not found"));
        String path = slot == 1 ? report.getPhoto1Path() : report.getPhoto2Path();
        if (isBlank(path)) throw new RuntimeException("Photo not found");
        return new FileSystemResource(path);
    }

    private Mission mission(Long id, User user) {
        return missionRepository.findByIdAndCompany(id, user.getCompany())
                .orElseThrow(() -> new RuntimeException("Mission not found"));
    }

    private User currentOperatorOrThrow() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Authenticated user not found");
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        if (user.getRole() != Role.DRONE_OPERATOR || user.getCompany() == null)
            throw new RuntimeException("Only drone operators can access agriculture reports");
        return user;
    }

    private void validate(AgricultureReportRequest r) {
        if (r.applicationDate == null) r.applicationDate = LocalDate.now();
        if (isBlank(r.cropType)) throw new IllegalArgumentException("Crop type is required");
        if (r.totalAreaTreatedHa == null || r.totalAreaTreatedHa.signum() <= 0)
            throw new IllegalArgumentException("Total treated area must be greater than zero");
        if (isBlank(r.targetProblem)) throw new IllegalArgumentException("Target pest/problem is required");
    }

    private void apply(AgricultureReport x, AgricultureReportRequest r) {
        x.setApplicationDate(r.applicationDate);
        x.setOperatorLicense(clean(r.operatorLicense)); x.setCropType(clean(r.cropType));
        x.setGrowthStage(clean(r.growthStage)); x.setTotalAreaTreatedHa(r.totalAreaTreatedHa);
        x.setTargetProblem(clean(r.targetProblem)); x.setApplicationStartTime(clean(r.applicationStartTime));
        x.setApplicationEndTime(clean(r.applicationEndTime)); x.setTemperatureC(r.temperatureC);
        x.setWindSpeedKmh(r.windSpeedKmh); x.setWindDirection(clean(r.windDirection));
        x.setRelativeHumidity(r.relativeHumidity); x.setSkyConditions(clean(r.skyConditions));
        x.setRainForecast(clean(r.rainForecast)); x.setTradeName(clean(r.tradeName));
        x.setRegistrationNumber(clean(r.registrationNumber)); x.setActiveIngredient(clean(r.activeIngredient));
        x.setTotalProductUsedL(r.totalProductUsedL); x.setTotalWaterVolumeL(r.totalWaterVolumeL);
        x.setProductRateLHa(r.productRateLHa); x.setWaterRateLHa(r.waterRateLHa); x.setAdjuvants(clean(r.adjuvants));
        x.setEquipmentUsed(clean(r.equipmentUsed)); x.setNozzleType(clean(r.nozzleType));
        x.setDropletSizeMicrons(r.dropletSizeMicrons); x.setPreHarvestIntervalDays(r.preHarvestIntervalDays);
        x.setRestrictedEntryIntervalHours(r.restrictedEntryIntervalHours); x.setBufferZoneNotes(clean(r.bufferZoneNotes));
        x.setCoverageQuality(clean(r.coverageQuality)); x.setCoverageObservations(clean(r.coverageObservations));
        x.setIncidentsNotes(clean(r.incidentsNotes)); x.setNextScoutDate(r.nextScoutDate);
        x.setApplicatorSignature(clean(r.applicatorSignature));
    }

    private String storePhoto(AgricultureReport report, MultipartFile file, String slot) throws IOException {
        if (!file.getContentType().startsWith("image/")) throw new IllegalArgumentException("Only image files are allowed");
        if (file.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("Each report photo must be 10 MB or smaller");
        Path dir = storageRoot.resolve(report.getMission().getMissionCode());
        Files.createDirectories(dir);
        String ext = extension(file.getOriginalFilename());
        Path target = dir.resolve(slot + "-" + UUID.randomUUID() + ext).normalize();
        if (!target.startsWith(dir)) throw new IllegalArgumentException("Invalid photo filename");
        file.transferTo(target);
        return target.toString();
    }

    private String generateCode() {
        return "AR-" + LocalDate.now().getYear() + "-" + String.format("%04d", reportRepository.count() + 1);
    }

    private AgricultureReportResponse draftResponse(Mission m) {
        AgricultureReportResponse r = new AgricultureReportResponse();
        r.missionId=m.getId(); r.missionCode=m.getMissionCode(); r.category=m.getCategory();
        r.farmId=m.getFarm().getId(); r.farmName=m.getFarm().getName(); if(m.getFarm().getAreaHectares()!=null) r.farmAreaHa=java.math.BigDecimal.valueOf(m.getFarm().getAreaHectares());
        r.farmBlockId=m.getFarmBlock().getId(); r.farmBlockName=m.getFarmBlock().getName();
        if (m.getFarmBlock().getAreaHectares()!=null) r.blockAreaHa=java.math.BigDecimal.valueOf(m.getFarmBlock().getAreaHectares());
        r.operatorId=m.getOperator().getId(); r.operatorName=(m.getOperator().getFirstName()+" "+m.getOperator().getLastName()).trim();
        r.applicationDate=m.getScheduledDate(); r.totalAreaTreatedHa=r.blockAreaHa; r.finalized=false;
        return r;
    }

    private AgricultureReportResponse toResponse(AgricultureReport x) {
        Mission m=x.getMission(); AgricultureReportResponse r=new AgricultureReportResponse();
        r.id=x.getId(); r.missionId=m.getId(); r.missionCode=m.getMissionCode(); r.category=m.getCategory();
        r.reportCode=x.getReportCode(); r.applicationDate=x.getApplicationDate(); r.operatorLicense=x.getOperatorLicense();
        r.cropType=x.getCropType(); r.growthStage=x.getGrowthStage(); r.totalAreaTreatedHa=x.getTotalAreaTreatedHa(); r.targetProblem=x.getTargetProblem();
        r.applicationStartTime=x.getApplicationStartTime(); r.applicationEndTime=x.getApplicationEndTime(); r.temperatureC=x.getTemperatureC();
        r.windSpeedKmh=x.getWindSpeedKmh(); r.windDirection=x.getWindDirection(); r.relativeHumidity=x.getRelativeHumidity();
        r.skyConditions=x.getSkyConditions(); r.rainForecast=x.getRainForecast(); r.tradeName=x.getTradeName();
        r.registrationNumber=x.getRegistrationNumber(); r.activeIngredient=x.getActiveIngredient(); r.totalProductUsedL=x.getTotalProductUsedL();
        r.totalWaterVolumeL=x.getTotalWaterVolumeL(); r.productRateLHa=x.getProductRateLHa(); r.waterRateLHa=x.getWaterRateLHa();
        r.adjuvants=x.getAdjuvants(); r.equipmentUsed=x.getEquipmentUsed(); r.nozzleType=x.getNozzleType();
        r.dropletSizeMicrons=x.getDropletSizeMicrons(); r.preHarvestIntervalDays=x.getPreHarvestIntervalDays();
        r.restrictedEntryIntervalHours=x.getRestrictedEntryIntervalHours(); r.bufferZoneNotes=x.getBufferZoneNotes();
        r.coverageQuality=x.getCoverageQuality(); r.coverageObservations=x.getCoverageObservations(); r.incidentsNotes=x.getIncidentsNotes();
        r.nextScoutDate=x.getNextScoutDate(); r.applicatorSignature=x.getApplicatorSignature(); r.finalized=x.isFinalized();
        r.createdAt=x.getCreatedAt(); r.updatedAt=x.getUpdatedAt();
        r.farmId=m.getFarm().getId(); r.farmName=m.getFarm().getName(); if(m.getFarm().getAreaHectares()!=null) r.farmAreaHa=java.math.BigDecimal.valueOf(m.getFarm().getAreaHectares());
        r.farmBlockId=m.getFarmBlock().getId(); r.farmBlockName=m.getFarmBlock().getName(); 
        if (m.getFarmBlock().getAreaHectares()!=null) r.blockAreaHa=java.math.BigDecimal.valueOf(m.getFarmBlock().getAreaHectares());
        if(m.getOperator()!=null){r.operatorId=m.getOperator().getId(); r.operatorName=(m.getOperator().getFirstName()+" "+m.getOperator().getLastName()).trim();}
        if(x.getPhoto1Path()!=null) r.photo1Url="/api/agriculture-reports/"+x.getId()+"/photos/1";
        if(x.getPhoto2Path()!=null) r.photo2Url="/api/agriculture-reports/"+x.getId()+"/photos/2";
        return r;
    }
    private String clean(String v){return v==null?null:v.trim().isEmpty()?null:v.trim();}
    private boolean isBlank(String v){return v==null||v.isBlank();}
    private String extension(String n){ if(n==null||!n.contains(".")) return ".jpg"; String e=n.substring(n.lastIndexOf('.')).toLowerCase(); return e.matches("\\.(jpg|jpeg|png|webp)$")?e:".jpg"; }
}
