package com.dmfs.operations.controller;
import com.dmfs.operations.dto.*;
import com.dmfs.operations.service.DroneOperatorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/drone-operator") @PreAuthorize("hasRole('DRONE_OPERATOR')")
public class DroneOperatorController {
 private final DroneOperatorService service; public DroneOperatorController(DroneOperatorService service){this.service=service;}
 @GetMapping("/operations") public List<OperationResponse> operations(Authentication a){return service.operations(a.getName());}
 @PostMapping("/operations/{id}/accept") public OperationalSurveyResponse accept(@PathVariable Long id,Authentication a){return service.accept(id,a.getName());}
 @PostMapping("/operations/{id}/reject") public void reject(@PathVariable Long id,@Valid @RequestBody RejectOperationRequest r,Authentication a){service.reject(id,r,a.getName());}
 @GetMapping("/surveys") public List<OperationalSurveyResponse> surveys(Authentication a){return service.surveys(a.getName());}
 @PostMapping("/surveys/{id}/start") public OperationalSurveyResponse start(@PathVariable Long id,Authentication a){return service.start(id,a.getName());}
 @PostMapping("/surveys/{id}/complete") public OperationalSurveyResponse complete(@PathVariable Long id,@Valid @RequestBody CompleteSurveyRequest r,Authentication a){return service.complete(id,r,a.getName());}
}
