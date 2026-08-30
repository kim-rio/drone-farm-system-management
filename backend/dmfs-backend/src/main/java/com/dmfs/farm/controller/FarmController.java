package com.dmfs.farm.controller;

import com.dmfs.farm.dto.FarmResponse;
import com.dmfs.farm.entity.Farm;
import com.dmfs.farm.service.FarmService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @PostMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<FarmResponse> createFarm(
            @PathVariable Long customerId,
            @RequestBody FarmRequest request
    ) {

        Farm farm = farmService.createFarm(
                customerId,
                request.name(),
                request.description(),
                request.latitude(),
                request.longitude(),
                request.areaHectares()
        );

        return ResponseEntity.ok(
                FarmResponse.from(farm)
        );
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<List<FarmResponse>> getCustomerFarms(
            @PathVariable Long customerId
    ) {

        List<FarmResponse> farms =
                farmService.getCustomerFarms(customerId)
                        .stream()
                        .map(FarmResponse::from)
                        .toList();

        return ResponseEntity.ok(farms);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<FarmResponse> getFarm(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                FarmResponse.from(
                        farmService.getFarm(id)
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<Void> deleteFarm(
            @PathVariable Long id
    ) {

        farmService.deleteFarm(id);

        return ResponseEntity.noContent().build();
    }

    public record FarmRequest(
            String name,
            String description,
            Double latitude,
            Double longitude,
            Double areaHectares
    ) {
    }
}