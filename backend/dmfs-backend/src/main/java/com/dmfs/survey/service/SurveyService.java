package com.dmfs.survey.service;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceRequestRepository;
import com.dmfs.survey.dto.CreateSurveyRequest;
import com.dmfs.survey.dto.SurveyResponse;
import com.dmfs.survey.dto.UpdateSurveyRequest;
import com.dmfs.survey.entity.SurveyStatus;
import com.dmfs.survey.entity.Surveyy;
import com.dmfs.survey.repository.SurveyyRepository;

@Service
public class SurveyService {

    private final SurveyyRepository surveyRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

    private final GeometryFactory geometryFactory =
            new GeometryFactory();


    public SurveyService(
            SurveyyRepository surveyRepository,
            ServiceRequestRepository serviceRequestRepository,
            UserRepository userRepository
    ) {
        this.surveyRepository = surveyRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.userRepository = userRepository;
    }


    // =========================================================
    // GET ALL SURVEYS
    // =========================================================

    @Transactional(readOnly = true)
    public List<SurveyResponse> getSurveys() {

        SubscriberCompany company =
                getCurrentUserCompany();

        return surveyRepository
                .findByCompanyOrderByCreatedAtDesc(company)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SurveyResponse> getMySurveys() {
        User user = getCurrentUser();
        if (user.getRole() != Role.DRONE_OPERATOR) {
            throw new RuntimeException("Only drone operators can access assigned surveys");
        }
        return surveyRepository.findByCompanyAndOperatorIdOrderByCreatedAtDesc(user.getCompany(), user.getId())
                .stream().map(this::toResponse).toList();
    }


    // =========================================================
    // GET SURVEY
    // =========================================================

    @Transactional(readOnly = true)
    public SurveyResponse getSurvey(Long id) {

        SubscriberCompany company =
                getCurrentUserCompany();

        Surveyy survey =
                surveyRepository
                        .findByIdAndCompany(id, company)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Survey not found"
                                )
                        );

        return toResponse(survey);
    }


    // =========================================================
    // CREATE SURVEY
    // =========================================================

    @Transactional
    public SurveyResponse createSurvey(
            CreateSurveyRequest request
    ) {

        SubscriberCompany company =
                getCurrentUserCompany();

        String surveyCode =
                request.getSurveyCode()
                        .trim()
                        .toUpperCase();

        if (surveyRepository
                .existsByCompanyAndSurveyCode(
                        company,
                        surveyCode
                )) {

            throw new RuntimeException(
                    "Survey code already exists"
            );
        }


        ServiceRequest serviceRequest =
                serviceRequestRepository
                        .findById(
                                request.getServiceRequestId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Service request not found"
                                )
                        );


        /*
         * The service request must belong
         * to the current company.
         */
        if (!serviceRequest
                .getCustomer()
                .getCompany()
                .getId()
                .equals(company.getId())) {

            throw new RuntimeException(
                    "Service request does not belong to your company"
            );
        }


        User operator =
                userRepository
                        .findById(
                                request.getOperatorId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Operator not found"
                                )
                        );


        validateOperator(
                operator,
                company
        );


        Surveyy survey = new Surveyy();

        survey.setSurveyCode(surveyCode);

        survey.setSurveyName(
                trimNullable(
                        request.getSurveyName()
                )
        );

        survey.setDescription(
                trimNullable(
                        request.getDescription()
                )
        );

        survey.setCompany(company);

        survey.setServiceRequest(
                serviceRequest
        );

        survey.setOperator(operator);

        survey.setStartedAt(
                request.getStartedAt()
        );

        survey.setEndedAt(
                request.getEndedAt()
        );

        survey.setStartLocation(
                createPoint(
                        request.getStartLatitude(),
                        request.getStartLongitude()
                )
        );

        survey.setEndLocation(
                createPoint(
                        request.getEndLatitude(),
                        request.getEndLongitude()
                )
        );

        survey.setStatus(
                SurveyStatus.DRAFT
        );


        /*
         * Customer, farm and block information
         * is derived from the service request.
         *
         * This prevents the client from creating
         * a survey against unrelated farm data.
         */
        return toResponse(
                surveyRepository.save(survey)
        );
    }


    // =========================================================
    // UPDATE SURVEY
    // =========================================================

    @Transactional
    public SurveyResponse updateSurvey(
            Long id,
            UpdateSurveyRequest request
    ) {

        SubscriberCompany company =
                getCurrentUserCompany();

        Surveyy survey =
                surveyRepository
                        .findByIdAndCompany(
                                id,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Survey not found"
                                )
                        );


        survey.setSurveyName(
                request.getSurveyName().trim()
        );

        survey.setDescription(
                trimNullable(
                        request.getDescription()
                )
        );


        if (request.getStartedAt() != null) {

            survey.setStartedAt(
                    request.getStartedAt()
            );
        }


        if (request.getEndedAt() != null) {

            survey.setEndedAt(
                    request.getEndedAt()
            );
        }


        Point startLocation =
                createPoint(
                        request.getStartLatitude(),
                        request.getStartLongitude()
                );

        if (startLocation != null) {

            survey.setStartLocation(
                    startLocation
            );
        }


        Point endLocation =
                createPoint(
                        request.getEndLatitude(),
                        request.getEndLongitude()
                );

        if (endLocation != null) {

            survey.setEndLocation(
                    endLocation
            );
        }


        if (request.getStatus() != null) {

            survey.setStatus(
                    request.getStatus()
            );
        }


        return toResponse(
                surveyRepository.save(survey)
        );
    }


    // =========================================================
    // START SURVEY
    // =========================================================

    @Transactional
    public SurveyResponse startSurvey(Long id) {

        SubscriberCompany company =
                getCurrentUserCompany();

        Surveyy survey =
                surveyRepository
                        .findByIdAndCompany(
                                id,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Survey not found"
                                )
                        );


        if (survey.getStatus() == SurveyStatus.CANCELLED) {

            throw new RuntimeException(
                    "Cancelled survey cannot be started"
            );
        }


        if (survey.getStatus() == SurveyStatus.COMPLETED) {

            throw new RuntimeException(
                    "Completed survey cannot be started"
            );
        }


        survey.setStatus(
                SurveyStatus.IN_PROGRESS
        );


        return toResponse(
                surveyRepository.save(survey)
        );
    }


    // =========================================================
    // COMPLETE SURVEY
    // =========================================================

    @Transactional
    public SurveyResponse completeSurvey(Long id) {

        SubscriberCompany company =
                getCurrentUserCompany();

        Surveyy survey =
                surveyRepository
                        .findByIdAndCompany(
                                id,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Survey not found"
                                )
                        );


        if (survey.getStatus() == SurveyStatus.CANCELLED) {

            throw new RuntimeException(
                    "Cancelled survey cannot be completed"
            );
        }


        if (survey.getStatus() == SurveyStatus.COMPLETED) {

            throw new RuntimeException(
                    "Survey is already completed"
            );
        }


        survey.setStatus(
                SurveyStatus.COMPLETED
        );


        return toResponse(
                surveyRepository.save(survey)
        );
    }


    // =========================================================
    // CANCEL SURVEY
    // =========================================================

    @Transactional
    public SurveyResponse cancelSurvey(Long id) {

        SubscriberCompany company =
                getCurrentUserCompany();

        Surveyy survey =
                surveyRepository
                        .findByIdAndCompany(
                                id,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Survey not found"
                                )
                        );

        survey.setStatus(
                SurveyStatus.CANCELLED
        );

        return toResponse(
                surveyRepository.save(survey)
        );
    }


    // =========================================================
    // VALIDATE OPERATOR
    // =========================================================

    private void validateOperator(
            User operator,
            SubscriberCompany company
    ) {

        if (operator.getRole()
                != Role.DRONE_OPERATOR) {

            throw new RuntimeException(
                    "Selected user is not a drone operator"
            );
        }


        if (operator.getCompany() == null
                || !operator
                .getCompany()
                .getId()
                .equals(company.getId())) {

            throw new RuntimeException(
                    "Operator does not belong to your company"
            );
        }


        if (!operator.isActive()) {

            throw new RuntimeException(
                    "Operator account is inactive"
            );
        }
    }


    // =========================================================
    // CURRENT COMPANY
    // =========================================================

    private SubscriberCompany getCurrentUserCompany() {

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


        return user.getCompany();
    }


    // =========================================================
    // CREATE POSTGIS POINT
    // =========================================================

    private Point createPoint(
            Double latitude,
            Double longitude
    ) {

        if (latitude == null
                || longitude == null) {

            return null;
        }


        if (latitude < -90
                || latitude > 90) {

            throw new RuntimeException(
                    "Invalid latitude"
            );
        }


        if (longitude < -180
                || longitude > 180) {

            throw new RuntimeException(
                    "Invalid longitude"
            );
        }


        /*
         * IMPORTANT:
         * PostGIS uses X = longitude
         * and Y = latitude.
         */
        Point point =
                geometryFactory.createPoint(
                        new Coordinate(
                                longitude,
                                latitude
                        )
                );

        point.setSRID(4326);

        return point;
    }


    // =========================================================
    // NULLABLE STRING
    // =========================================================

    private String trimNullable(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private SurveyResponse toResponse(
            Surveyy survey
    ) {

        SurveyResponse response =
                new SurveyResponse();

        response.setId(
                survey.getId()
        );

        response.setSurveyCode(
                survey.getSurveyCode()
        );

        response.setSurveyName(
                survey.getSurveyName()
        );

        response.setDescription(
                survey.getDescription()
        );

        response.setStartedAt(
                survey.getStartedAt() != null
                        ? survey.getStartedAt().toLocalDateTime()
                        : null
        );

        response.setEndedAt(
                survey.getEndedAt() != null
                        ? survey.getEndedAt().toLocalDateTime()
                        : null
        );

        response.setStatus(
                survey.getStatus()
        );

        response.setCreatedAt(
                survey.getCreatedAt() != null
                        ? survey.getCreatedAt().toLocalDateTime()
                        : null
        );

        response.setUpdatedAt(
                survey.getUpdatedAt() != null
                        ? survey.getUpdatedAt().toLocalDateTime()
                        : null
        );


        if (survey.getServiceRequest() != null) {

            ServiceRequest request =
                    survey.getServiceRequest();

            response.setServiceRequestId(
                    request.getId()
            );
        }


        if (survey.getOperator() != null) {

            User operator =
                    survey.getOperator();

            SurveyResponse.OperatorInfo info =
                    new SurveyResponse.OperatorInfo();

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


        if (survey.getStartLocation() != null) {

            response.setStartLatitude(
                    survey.getStartLocation()
                            .getY()
            );

            response.setStartLongitude(
                    survey.getStartLocation()
                            .getX()
            );
        }


        if (survey.getEndLocation() != null) {

            response.setEndLatitude(
                    survey.getEndLocation()
                            .getY()
            );

            response.setEndLongitude(
                    survey.getEndLocation()
                            .getX()
            );
        }


        return response;
    }

   private User getCurrentUser() {

    Authentication authentication =
            SecurityContextHolder
                    .getContext()
                    .getAuthentication();

    if (authentication == null
            || authentication.getName() == null
            || authentication.getName().isBlank()) {

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
}
