package com.dmfs.drone.controller;

import com.dmfs.drone.dto.DroneRequest;
import com.dmfs.drone.dto.DroneResponse;
import com.dmfs.drone.dto.DroneStatusRequest;
import com.dmfs.drone.service.DroneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
public class DroneController {

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @PostMapping
    public ResponseEntity<DroneResponse> createDrone(
            @Valid @RequestBody DroneRequest request) {

        DroneResponse response = droneService.createDrone(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DroneResponse>> getAllDrones() {

        return ResponseEntity.ok(
                droneService.getAllDrones()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DroneResponse> getDroneById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                droneService.getDroneById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<DroneResponse> updateDrone(
            @PathVariable Long id,
            @Valid @RequestBody DroneRequest request) {

        return ResponseEntity.ok(
                droneService.updateDrone(id, request)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DroneResponse> updateDroneStatus(
            @PathVariable Long id,
            @Valid @RequestBody DroneStatusRequest request) {

        return ResponseEntity.ok(
                droneService.updateDroneStatus(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrone(
            @PathVariable Long id) {

        droneService.deleteDrone(id);

        return ResponseEntity.noContent().build();
    }
}