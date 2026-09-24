package com.dmfs.mission.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.client.entity.Client;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.service.CompanyDocumentNumberService;
import com.dmfs.farm.entity.Block;
import com.dmfs.farm.entity.Farm;
import com.dmfs.mission.dto.CreateMissionRequest;
import com.dmfs.mission.dto.MissionResponse;
import com.dmfs.mission.dto.UpdateMissionRequest;
import com.dmfs.mission.entity.Mission;
import com.dmfs.agriculture.entity.AgricultureReport;
import com.dmfs.agriculture.repository.AgricultureReportRepository;
import com.dmfs.mission.entity.MissionStatus;
import com.dmfs.mission.repository.MissionRepository;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceRequestRepository;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final AgricultureReportRepository agricultureReportRepository;
    private final CompanyDocumentNumberService documentNumberService;

    public MissionService(
            MissionRepository missionRepository,
            ServiceRequestRepository serviceRequestRepository,
            UserRepository userRepository,
            AgricultureReportRepository agricultureReportRepository,
            CompanyDocumentNumberService documentNumberService
    ) {
        this.missionRepository = missionRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.userRepository = userRepository;
        this.agricultureReportRepository = agricultureReportRepository;
        this.documentNumberService = documentNumberService;
    }


    // =========================================================
    // GET ALL MISSIONS
    // =========================================================

    @Transactional(readOnly = true)
    public List<MissionResponse> getMissions() {

        SubscriberCompany company =
                getCurrentCompany();

        return missionRepository
                .findByCompanyOrderByCreatedAtDesc(company)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Operator-scoped view: an operator never receives a colleague's work. */
    @Transactional(readOnly = true)
    public List<MissionResponse> getMyMissions() {
        User user = getCurrentUser();
        if (user.getRole() != Role.DRONE_OPERATOR) {
            throw new RuntimeException("Only drone operators can access assigned missions");
        }
        return missionRepository.findByCompanyAndOperatorOrderByCreatedAtDesc(user.getCompany(), user)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public MissionResponse acceptMission(Long id) {
        return transitionMyMission(id, MissionStatus.ASSIGNED, MissionStatus.ACCEPTED);
    }

    @Transactional
    public MissionResponse startMission(Long id) {
        return transitionMyMission(id, MissionStatus.ACCEPTED, MissionStatus.IN_PROGRESS);
    }

    @Transactional
    public MissionResponse completeMission(Long id) {
        return transitionMyMission(id, MissionStatus.IN_PROGRESS, MissionStatus.COMPLETED);
    }
    @Transactional
public void completeMissionForServiceRequest(Long serviceRequestId) {

    if (serviceRequestId == null) {
        throw new IllegalArgumentException("Service request ID is required");
    }

    User user = getCurrentUser();

    if (user.getRole() != Role.DRONE_OPERATOR) {
        throw new RuntimeException(
                "Only drone operators can complete field missions"
        );
    }

    ServiceRequest serviceRequest = serviceRequestRepository
            .findById(serviceRequestId)
            .orElseThrow(() ->
                    new RuntimeException("Service request not found")
            );

    Mission mission = missionRepository
            .findByServiceRequest(serviceRequest)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Mission not found for service request"
                    )
            );

    if (mission.getOperator() == null
            || !mission.getOperator().getId().equals(user.getId())) {

        throw new RuntimeException(
                "This mission is not assigned to you"
        );
    }

    if (mission.getCompany() == null
            || user.getCompany() == null
            || !mission.getCompany().getId().equals(user.getCompany().getId())) {

        throw new RuntimeException(
                "This mission does not belong to your company"
        );
    }

    if (mission.getStatus() != MissionStatus.IN_PROGRESS) {
        throw new RuntimeException(
                "Mission must be IN_PROGRESS before it can be completed"
        );
    }

    if ("AGRICULTURE".equals(mission.getCategory())) {
        boolean finalized =
                agricultureReportRepository
                        .findByMission(mission)
                        .map(report -> report.isFinalized())
                        .orElse(false);

        if (!finalized) {
            throw new RuntimeException(
                    "Agriculture mission requires a finalized field application report before completion"
            );
        }
    }

    mission.setStatus(MissionStatus.COMPLETED);

    missionRepository.save(mission);
}


    // =========================================================
    // GET MISSION BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public MissionResponse getMission(Long id) {

        SubscriberCompany company =
                getCurrentCompany();

        Mission mission =
                missionRepository
                        .findByIdAndCompany(id, company)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Mission not found"
                                )
                        );

        return toResponse(mission);
    }


    // =========================================================
    // CREATE MISSION
    // =========================================================

    @Transactional
    public MissionResponse createMission(
            CreateMissionRequest request
    ) {

        SubscriberCompany company =
                getCurrentCompany();

        if (request.getScheduledDate() == null) {
            throw new RuntimeException(
                    "Scheduled date is required"
            );
        }

        if (request.getScheduledDate()
                .isBefore(LocalDate.now())) {

            throw new RuntimeException(
                    "Scheduled date cannot be in the past"
            );
        }


        // -----------------------------------------------------
        // Find service request
        // -----------------------------------------------------

        ServiceRequest serviceRequest =
                serviceRequestRepository
                        .findByIdAndCustomerCompanyId(
                                request.getServiceRequestId(),
                                company.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Service request not found for your company"
                                )
                        );


        // -----------------------------------------------------
        // Verify service request belongs to current company
        // -----------------------------------------------------

        validateServiceRequestCompany(
                serviceRequest,
                company
        );

        String category = normalizeMissionCategory(request.getCategory());
        String serviceCategory = normalizeMissionCategory(
                serviceRequest.getServiceCatalogue().getCategory()
        );

        if (!category.equals(serviceCategory)) {
            throw new IllegalArgumentException(
                    "Mission category must match the selected service category"
            );
        }


        // -----------------------------------------------------
        // Prevent duplicate mission
        // -----------------------------------------------------

        if (missionRepository
                .existsByServiceRequest(serviceRequest)) {

            throw new RuntimeException(
                    "A mission already exists for this service request"
            );
        }


        // -----------------------------------------------------
        // Create mission
        // -----------------------------------------------------

        Mission mission = new Mission();

        mission.setMissionCode(
                documentNumberService.nextMissionNumber(company)
        );

        mission.setCompany(company);

        mission.setServiceRequest(
                serviceRequest
        );

        mission.setCustomer(
                serviceRequest.getCustomer()
        );

        mission.setCategory(category);

        mission.setFarm(
                serviceRequest.getFarm()
        );

        mission.setFarmBlock(
                serviceRequest.getFarmBlock()
        );

        mission.setScheduledDate(
                request.getScheduledDate()
        );

        mission.setNotes(
                cleanNotes(request.getNotes())
        );

        User operator = userRepository
                .findByIdAndCompany(request.getOperatorId(), company)
                .orElseThrow(() -> new RuntimeException("Operator not found"));

        if (operator.getRole() != Role.DRONE_OPERATOR || !operator.isActive()) {
            throw new RuntimeException("Selected drone operator is not available");
        }

        mission.setOperator(operator);

        mission.setStatus(
                MissionStatus.ASSIGNED
        );


        Mission saved =
                missionRepository.save(mission);

        return toResponse(saved);
    }


    // =========================================================
    // UPDATE MISSION
    // =========================================================

    @Transactional
    public MissionResponse updateMission(
            Long id,
            UpdateMissionRequest request
    ) {

        SubscriberCompany company =
                getCurrentCompany();

        Mission mission =
                missionRepository
                        .findByIdAndCompany(id, company)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Mission not found"
                                )
                        );


        if (request.getScheduledDate() != null) {

            if (request.getScheduledDate()
                    .isBefore(LocalDate.now())) {

                throw new RuntimeException(
                        "Scheduled date cannot be in the past"
                );
            }

            mission.setScheduledDate(
                    request.getScheduledDate()
            );
        }


        if (request.getStatus() != null) {

            validateStatusChange(
                    mission.getStatus(),
                    request.getStatus()
            );

            mission.setStatus(
                    request.getStatus()
            );
        }


        if (request.getNotes() != null) {

            mission.setNotes(
                    cleanNotes(request.getNotes())
            );
        }


        return toResponse(
                missionRepository.save(mission)
        );
    }


    // =========================================================
    // CANCEL MISSION
    // =========================================================

    @Transactional
    public MissionResponse cancelMission(Long id) {

        SubscriberCompany company =
                getCurrentCompany();

        Mission mission =
                missionRepository
                        .findByIdAndCompany(id, company)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Mission not found"
                                )
                        );

        if (mission.getStatus()
                == MissionStatus.COMPLETED) {

            throw new RuntimeException(
                    "A completed mission cannot be cancelled"
            );
        }

        mission.setStatus(
                MissionStatus.CANCELLED
        );

        return toResponse(
                missionRepository.save(mission)
        );
    }


    // =========================================================
    // ASSIGN OPERATOR
    // =========================================================

    @Transactional
    public MissionResponse assignOperator(
            Long missionId,
            Long operatorId
    ) {

        SubscriberCompany company =
                getCurrentCompany();

        Mission mission =
                missionRepository
                        .findByIdAndCompany(
                                missionId,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Mission not found"
                                )
                        );


        User operator =
                userRepository
                        .findByIdAndCompany(
                                operatorId,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Operator not found"
                                )
                        );


        if (operator.getRole()
                != Role.DRONE_OPERATOR) {

            throw new RuntimeException(
                    "Selected user is not a drone operator"
            );
        }


        if (!operator.isActive()) {

            throw new RuntimeException(
                    "Selected drone operator is inactive"
            );
        }


        if (mission.getStatus()
                == MissionStatus.COMPLETED
                || mission.getStatus()
                == MissionStatus.CANCELLED) {

            throw new RuntimeException(
                    "Operator cannot be assigned to this mission"
            );
        }


        mission.setOperator(operator);

        mission.setStatus(
                MissionStatus.ASSIGNED
        );


        return toResponse(
                missionRepository.save(mission)
        );
    }
    // =========================================================
// GET DRONE OPERATORS
// =========================================================

@Transactional(readOnly = true)
public List<MissionResponse.OperatorInfo> getDroneOperators() {

    SubscriberCompany company = getCurrentCompany();

    List<User> operators =
            userRepository
                    .findByCompanyAndRoleInOrderByFirstNameAsc(
                            company,
                            List.of(Role.DRONE_OPERATOR)
                    );

    return operators.stream()
            .filter(User::isActive)
            .map(user -> {

                MissionResponse.OperatorInfo info =
                        new MissionResponse.OperatorInfo();

                info.setId(user.getId());
                info.setFirstName(user.getFirstName());
                info.setLastName(user.getLastName());
                info.setEmail(user.getEmail());

                return info;
            })
            .toList();
}


    // =========================================================
    // REMOVE OPERATOR
    // =========================================================

    @Transactional
    public MissionResponse removeOperator(
            Long missionId
    ) {

        SubscriberCompany company =
                getCurrentCompany();

        Mission mission =
                missionRepository
                        .findByIdAndCompany(
                                missionId,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Mission not found"
                                )
                        );


        if (mission.getStatus()
                == MissionStatus.IN_PROGRESS) {

            throw new RuntimeException(
                    "Operator cannot be removed from a mission in progress"
            );
        }


        if (mission.getStatus()
                == MissionStatus.COMPLETED) {

            throw new RuntimeException(
                    "Operator cannot be removed from a completed mission"
            );
        }


        mission.setOperator(null);

        mission.setStatus(
                MissionStatus.PLANNED
        );


        return toResponse(
                missionRepository.save(mission)
        );
    }


    // =========================================================
    // SERVICE REQUEST VALIDATION
    // =========================================================

    private void validateServiceRequestCompany(
            ServiceRequest serviceRequest,
            SubscriberCompany company
    ) {

        Client customer =
                serviceRequest.getCustomer();

        if (customer == null) {

            throw new RuntimeException(
                    "Service request has no customer"
            );
        }


        if (customer.getCompany() == null
                || !customer.getCompany()
                .getId()
                .equals(company.getId())) {

            throw new RuntimeException(
                    "Service request does not belong to your company"
            );
        }

        if (serviceRequest.getServiceCatalogue() == null
                || serviceRequest.getServiceCatalogue().getCompany() == null
                || !serviceRequest.getServiceCatalogue().getCompany().getId().equals(company.getId())) {
            throw new RuntimeException(
                    "Service request uses a service catalogue outside your company"
            );
        }


        Farm farm =
                serviceRequest.getFarm();

        if (farm == null
                || farm.getCustomer() == null
                || !farm.getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new RuntimeException(
                    "Service request has an invalid farm"
            );
        }


        Block block =
                serviceRequest.getFarmBlock();

        if (block == null
                || block.getFarm() == null
                || !block.getFarm()
                .getId()
                .equals(farm.getId())) {

            throw new RuntimeException(
                    "Service request has an invalid farm block"
            );
        }
    }


    // =========================================================
    // STATUS TRANSITIONS
    // =========================================================

    private void validateStatusChange(
            MissionStatus current,
            MissionStatus next
    ) {

        if (current == next) {
            return;
        }


        if (current == MissionStatus.COMPLETED) {

            throw new RuntimeException(
                    "Completed missions cannot change status"
            );
        }


        if (current == MissionStatus.CANCELLED) {

            throw new RuntimeException(
                    "Cancelled missions cannot change status"
            );
        }


        switch (current) {

            case PLANNED -> {

                if (next != MissionStatus.ASSIGNED
                        && next != MissionStatus.CANCELLED) {

                    throw invalidTransition(
                            current,
                            next
                    );
                }
            }


            case ASSIGNED -> {

                if (next != MissionStatus.ACCEPTED
                        && next != MissionStatus.PLANNED
                        && next != MissionStatus.CANCELLED) {

                    throw invalidTransition(
                            current,
                            next
                    );
                }
            }


            case ACCEPTED -> {

                if (next != MissionStatus.IN_PROGRESS
                        && next != MissionStatus.CANCELLED) {

                    throw invalidTransition(
                            current,
                            next
                    );
                }
            }


            case IN_PROGRESS -> {

                if (next != MissionStatus.COMPLETED
                        && next != MissionStatus.CANCELLED) {

                    throw invalidTransition(
                            current,
                            next
                    );
                }
            }


            default -> {
                // COMPLETED and CANCELLED handled above
            }
        }
    }


    private RuntimeException invalidTransition(
            MissionStatus current,
            MissionStatus next
    ) {

        return new RuntimeException(
                "Invalid mission status transition: "
                        + current
                        + " -> "
                        + next
        );
    }


    private String normalizeMissionCategory(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Mission category is required"
            );
        }

        String normalized = value.trim().toUpperCase();

        if (!"MINING".equals(normalized)
                && !"AGRICULTURE".equals(normalized)) {
            throw new IllegalArgumentException(
                    "Mission category must be MINING or AGRICULTURE"
            );
        }

        return normalized;
    }


    // =========================================================
    // COMPANY FROM AUTHENTICATED USER
    // =========================================================

    private SubscriberCompany getCurrentCompany() {

        return getCurrentUser().getCompany();
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null
                || authentication.getName() == null) {

            throw new RuntimeException(
                    "Authenticated user not found"
            );
        }


        User user =
                userRepository
                        .findByEmail(
                                authentication.getName()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Authenticated user not found"
                                )
                        );


        if (user.getCompany() == null) {

            throw new RuntimeException(
                    "User is not assigned to a company"
            );
        }


        return user;
    }

    private MissionResponse transitionMyMission(Long id, MissionStatus expected, MissionStatus next) {
        User user = getCurrentUser();
        if (user.getRole() != Role.DRONE_OPERATOR) {
            throw new RuntimeException("Only drone operators can update field mission status");
        }
        Mission mission = missionRepository.findByIdAndCompany(id, user.getCompany())
                .orElseThrow(() -> new RuntimeException("Mission not found"));
        if (mission.getOperator() == null || !mission.getOperator().getId().equals(user.getId())) {
            throw new RuntimeException("This mission is not assigned to you");
        }
        if (mission.getStatus() != expected) {
            throw new RuntimeException("Mission must be " + expected + " before it can be " + next);
        }

        if (next == MissionStatus.COMPLETED
                && "AGRICULTURE".equals(mission.getCategory())) {
            boolean finalized = agricultureReportRepository
                    .findByMission(mission)
                    .map(report -> report.isFinalized())
                    .orElse(false);

            if (!finalized) {
                throw new RuntimeException(
                        "Agriculture mission requires a finalized field application report before completion"
                );
            }
        }

        mission.setStatus(next);
        return toResponse(missionRepository.save(mission));
    }


    // =========================================================
    // NOTES
    // =========================================================

    private String cleanNotes(String notes) {

        if (notes == null) {
            return null;
        }

        String cleaned =
                notes.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private MissionResponse toResponse(
            Mission mission
    ) {

        MissionResponse response =
                new MissionResponse();


        response.setId(
                mission.getId()
        );

        response.setMissionCode(
                mission.getMissionCode()
        );

        response.setStatus(
                mission.getStatus()
        );

        response.setCategory(
                mission.getCategory()
        );

        response.setScheduledDate(
                mission.getScheduledDate()
        );

        response.setNotes(
                mission.getNotes()
        );

        response.setCreatedAt(
                mission.getCreatedAt()
        );

        response.setUpdatedAt(
                mission.getUpdatedAt()
        );


        // -----------------------------------------------------
        // Service Request
        // -----------------------------------------------------

        ServiceRequest serviceRequest =
                mission.getServiceRequest();

        if (serviceRequest != null) {

            MissionResponse.ServiceRequestInfo info =
                    new MissionResponse.ServiceRequestInfo();

            info.setId(
                    serviceRequest.getId()
            );

            info.setRequestedDate(
                    serviceRequest.getRequestedDate()
            );

            info.setStatus(
                    serviceRequest.getStatus()
            );

            response.setServiceRequest(info);
        }


        // -----------------------------------------------------
        // Customer
        // -----------------------------------------------------

        Client customer =
                mission.getCustomer();

        if (customer != null) {

            MissionResponse.CustomerInfo info =
                    new MissionResponse.CustomerInfo();

            info.setId(
                    customer.getId()
            );

            info.setClientCode(
                    customer.getClientCode()
            );

            info.setType(
                    customer.getType() != null
                            ? customer.getType().name()
                            : null
            );

            info.setStatus(
                    customer.getStatus() != null
                            ? customer.getStatus().name()
                            : null
            );

            response.setCustomer(info);
        }


        // -----------------------------------------------------
        // Farm
        // -----------------------------------------------------

        Farm farm =
                mission.getFarm();

        if (farm != null) {

            MissionResponse.FarmInfo info =
                    new MissionResponse.FarmInfo();

            info.setId(
                    farm.getId()
            );

            info.setName(
                    farm.getName()
            );

            info.setAreaHectares(
                    farm.getAreaHectares()
            );

            response.setFarm(info);
        }


        // -----------------------------------------------------
        // Block
        // -----------------------------------------------------

        Block block =
                mission.getFarmBlock();

        if (block != null) {

            MissionResponse.BlockInfo info =
                    new MissionResponse.BlockInfo();

            info.setId(
                    block.getId()
            );

            info.setName(
                    block.getName()
            );

            info.setAreaHectares(
                    block.getAreaHectares()
            );

            info.setCenterLatitude(
                    block.getCenterLatitude()
            );

            info.setCenterLongitude(
                    block.getCenterLongitude()
            );

            response.setFarmBlock(info);
        }


        // -----------------------------------------------------
        // Operator
        // -----------------------------------------------------

        User operator =
                mission.getOperator();

        if (operator != null) {

            MissionResponse.OperatorInfo info =
                    new MissionResponse.OperatorInfo();

            info.setId(
                    operator.getId()
            );

            info.setFirstName(
                    operator.getFirstName()
            );

            info.setLastName(
                    operator.getLastName()
            );

            info.setEmail(
                    operator.getEmail()
            );

            response.setOperator(info);
        }


        // -----------------------------------------------------
        // Drone
        // -----------------------------------------------------

        if (mission.getDrone() != null) {

            MissionResponse.DroneInfo info =
                    new MissionResponse.DroneInfo();

            info.setId(
                    mission.getDrone().getId()
            );

            info.setName(
                    mission.getDrone().getName()
            );

            info.setSerialNumber(
                    mission.getDrone().getSerialNumber()
            );

            info.setModel(
                    mission.getDrone().getModel()
            );

            info.setStatus(
                    mission.getDrone().getStatus() != null
                            ? mission.getDrone()
                                    .getStatus()
                                    .name()
                            : null
            );

            response.setDrone(info);
        }


        return response;
    }
}
