package com.dmfs.operations.service;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.client.entity.*;
import com.dmfs.geologist.entity.Survey;
import com.dmfs.geologist.repository.SurveyRepository;
import com.dmfs.operations.dto.*;
import com.dmfs.operations.entity.ManagementNotification;
import com.dmfs.operations.repository.ManagementNotificationRepository;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceRequestRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DroneOperatorService {
 private final ServiceRequestRepository requests; private final SurveyRepository surveys; private final UserRepository users; private final ManagementNotificationRepository notifications;
 public DroneOperatorService(ServiceRequestRepository requests, SurveyRepository surveys, UserRepository users, ManagementNotificationRepository notifications){this.requests=requests;this.surveys=surveys;this.users=users;this.notifications=notifications;}
 @Transactional(readOnly=true) public List<OperationResponse> operations(String email){User u=user(email); return requests.findByStatusIgnoreCaseOrOperatorIdOrderByCreatedAtDesc("PENDING",u.getId()).stream().map(this::operation).toList();}
 @Transactional public OperationalSurveyResponse accept(Long requestId,String email){User u=user(email); ServiceRequest r=request(requestId); if(!"PENDING".equalsIgnoreCase(r.getStatus())) throw new IllegalArgumentException("This operation is no longer available"); if(u.getCompany()==null) throw new IllegalArgumentException("Drone operator has no company assignment"); r.setOperator(u);r.setStatus("ACCEPTED"); Survey s=new Survey();s.setCompany(u.getCompany());s.setServiceRequest(r);s.setOperator(u);s.setSurveyCode("SUR-"+System.currentTimeMillis());s.setSurveyName(r.getServiceCatalogue().getName());s.setDescription(r.getNotes());s.setStatus("SCHEDULED");surveys.save(s); notifyManagement(u,"OPERATION_ACCEPTED", "Drone operator accepted service request #"+r.getId()+"; survey "+s.getSurveyCode()+" is scheduled.");return survey(s);}
 @Transactional public void reject(Long requestId,RejectOperationRequest body,String email){User u=user(email);ServiceRequest r=request(requestId);if(!"PENDING".equalsIgnoreCase(r.getStatus())) throw new IllegalArgumentException("This operation is no longer available");r.setOperator(u);r.setStatus("REJECTED");r.setOperatorDecisionReason(body.reason().trim());notifyManagement(u,"OPERATION_REJECTED","Drone operator rejected service request #"+r.getId()+": "+body.reason().trim());}
 @Transactional(readOnly=true) public List<OperationalSurveyResponse> surveys(String email){return surveys.findByOperatorIdOrderByStartedAtDesc(user(email).getId()).stream().map(this::survey).toList();}
 @Transactional public OperationalSurveyResponse start(Long surveyId,String email){Survey s=assignedSurvey(surveyId,email);if(!"SCHEDULED".equalsIgnoreCase(s.getStatus())) throw new IllegalArgumentException("Only scheduled surveys can be started");s.setStatus("IN_PROGRESS");s.setStartedAt(OffsetDateTime.now());s.getServiceRequest().setStatus("IN_PROGRESS");notifyManagement(s.getOperator(),"SURVEY_STARTED","Survey "+s.getSurveyCode()+" has started.");return survey(s);}
 @Transactional public OperationalSurveyResponse complete(Long surveyId,CompleteSurveyRequest body,String email){if(body.minLatitude()>body.maxLatitude()||body.minLongitude()>body.maxLongitude())throw new IllegalArgumentException("Minimum coordinates must be less than maximum coordinates");Survey s=assignedSurvey(surveyId,email);if(!"IN_PROGRESS".equalsIgnoreCase(s.getStatus()))throw new IllegalArgumentException("Only in-progress surveys can be completed");OffsetDateTime now=OffsetDateTime.now();surveys.completeWithBounds(s.getId(),body.equipmentUsed().trim(),now,body.minLatitude(),body.minLongitude(),body.maxLatitude(),body.maxLongitude());s.setStatus("COMPLETED");s.setEquipmentUsed(body.equipmentUsed().trim());s.setCompletedAt(now);s.setEndedAt(now);s.getServiceRequest().setStatus("COMPLETED");notifyManagement(s.getOperator(),"SURVEY_COMPLETED","Survey "+s.getSurveyCode()+" completed. Equipment: "+body.equipmentUsed().trim());return survey(s);}
 private User user(String email){return users.findByEmail(email).orElseThrow(()->new AccessDeniedException("Authenticated operator was not found"));}
 private ServiceRequest request(Long id){return requests.findById(id).orElseThrow(()->new IllegalArgumentException("Service request not found: "+id));}
 private Survey assignedSurvey(Long id,String email){Survey s=surveys.findById(id).orElseThrow(()->new IllegalArgumentException("Survey not found: "+id));if(!s.getOperator().getId().equals(user(email).getId()))throw new AccessDeniedException("This survey is not assigned to you");return s;}
 private void notifyManagement(User operator,String type,String message){if(operator.getCompany()==null)return;ManagementNotification n=new ManagementNotification();n.setCompany(operator.getCompany());n.setNotificationType(type);n.setMessage(message);n.setCreatedAt(OffsetDateTime.now());notifications.save(n);}
 private OperationResponse operation(ServiceRequest r){return new OperationResponse(r.getId(),clientName(r.getCustomer()),r.getFarm().getName(),r.getFarmBlock().getName(),r.getServiceCatalogue().getName(),r.getRequestedDate(),r.getNotes(),r.getStatus(),r.getOperatorDecisionReason());}
 private OperationalSurveyResponse survey(Survey s){ServiceRequest r=s.getServiceRequest();return new OperationalSurveyResponse(s.getId(),s.getSurveyCode(),s.getSurveyName(),clientName(r.getCustomer()),r.getFarm().getName(),r.getFarmBlock().getName(),s.getStatus(),s.getStartedAt(),s.getCompletedAt(),s.getEquipmentUsed());}
 private String clientName(Client c){if(c.getCompanyProfile()!=null && c.getCompanyProfile().getCompanyName()!=null)return c.getCompanyProfile().getCompanyName(); if(c.getIndividualProfile()!=null)return c.getIndividualProfile().getFirstName()+" "+c.getIndividualProfile().getLastName();return c.getClientCode();}
}
