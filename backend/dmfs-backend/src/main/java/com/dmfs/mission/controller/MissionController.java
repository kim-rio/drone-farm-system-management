package com.dmfs.mission.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dmfs.mission.dto.CreateMissionRequest;
import com.dmfs.mission.dto.MissionResponse;
import com.dmfs.mission.dto.UpdateMissionRequest;
import com.dmfs.mission.service.MissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(
            MissionService missionService
    ) {
        this.missionService = missionService;
    }


    // =========================================================
    // GET ALL MISSIONS
    // =========================================================

    @GetMapping
    public List<MissionResponse> getMissions() {

        return missionService.getMissions();
    }

    @GetMapping("/my")
    public List<MissionResponse> getMyMissions() {
        return missionService.getMyMissions();
    }

    @PatchMapping("/{id}/accept")
    public MissionResponse acceptMission(@PathVariable Long id) {
        return missionService.acceptMission(id);
    }

    @PatchMapping("/{id}/start")
    public MissionResponse startMission(@PathVariable Long id) {
        return missionService.startMission(id);
    }

    @PatchMapping("/{id}/complete")
    public MissionResponse completeMission(@PathVariable Long id) {
        return missionService.completeMission(id);
    }
    // =========================================================
    // GET DRONE OPERATORS
    // =========================================================

@GetMapping("/operators")
public List<MissionResponse.OperatorInfo> getDroneOperators() {
    return missionService.getDroneOperators();
}


    // =========================================================
    // GET MISSION BY ID
    // =========================================================

    @GetMapping("/{id}")
    public MissionResponse getMission(
            @PathVariable Long id
    ) {

        return missionService.getMission(id);
    }


    // =========================================================
    // CREATE MISSION
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MissionResponse createMission(
            @Valid @RequestBody CreateMissionRequest request
    ) {

        return missionService.createMission(request);
    }


    // =========================================================
    // UPDATE MISSION
    // =========================================================

    @PutMapping("/{id}")
    public MissionResponse updateMission(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMissionRequest request
    ) {

        return missionService.updateMission(
                id,
                request
        );
    }


    // =========================================================
    // CANCEL MISSION
    // =========================================================

    @PatchMapping("/{id}/cancel")
    public MissionResponse cancelMission(
            @PathVariable Long id
    ) {

        return missionService.cancelMission(id);
    }


    // =========================================================
    // ASSIGN DRONE OPERATOR
    // =========================================================

    @PatchMapping("/{missionId}/operator/{operatorId}")
    public MissionResponse assignOperator(
            @PathVariable Long missionId,
            @PathVariable Long operatorId
    ) {

        return missionService.assignOperator(
                missionId,
                operatorId
        );
    }
    


    // =========================================================
    // REMOVE DRONE OPERATOR
    // =========================================================

    @DeleteMapping("/{missionId}/operator")
    public MissionResponse removeOperator(
            @PathVariable Long missionId
    ) {

        return missionService.removeOperator(
                missionId
        );
    }
}
