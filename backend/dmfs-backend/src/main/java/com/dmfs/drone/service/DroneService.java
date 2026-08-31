package com.dmfs.drone.service;

import com.dmfs.drone.dto.DroneRequest;
import com.dmfs.drone.dto.DroneResponse;
import com.dmfs.drone.dto.DroneStatusRequest;
import com.dmfs.drone.entity.Drone;
import com.dmfs.drone.entity.DroneStatus;
import com.dmfs.drone.repository.DroneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DroneService {

    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    @Transactional
    public DroneResponse createDrone(DroneRequest request) {

        if (droneRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new IllegalArgumentException(
                    "Drone with serial number already exists"
            );
        }

        Drone drone = new Drone();

        drone.setName(request.getName());
        drone.setSerialNumber(request.getSerialNumber());
        drone.setModel(request.getModel());
        drone.setManufacturer(request.getManufacturer());
        drone.setDroneType(request.getDroneType());
        drone.setPurchaseDate(request.getPurchaseDate());
        drone.setStatus(DroneStatus.AVAILABLE);

        Drone savedDrone = droneRepository.save(drone);

        return toResponse(savedDrone);
    }

    @Transactional(readOnly = true)
    public List<DroneResponse> getAllDrones() {

        return droneRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DroneResponse getDroneById(Long id) {

        Drone drone = droneRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Drone not found with id: " + id
                        )
                );

        return toResponse(drone);
    }

    @Transactional
    public DroneResponse updateDrone(
            Long id,
            DroneRequest request
    ) {

        Drone drone = droneRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Drone not found with id: " + id
                        )
                );

        if (!drone.getSerialNumber().equals(request.getSerialNumber())
                && droneRepository.existsBySerialNumber(
                        request.getSerialNumber())) {

            throw new IllegalArgumentException(
                    "Drone with serial number already exists"
            );
        }

        drone.setName(request.getName());
        drone.setSerialNumber(request.getSerialNumber());
        drone.setModel(request.getModel());
        drone.setManufacturer(request.getManufacturer());
        drone.setDroneType(request.getDroneType());
        drone.setPurchaseDate(request.getPurchaseDate());

        return toResponse(droneRepository.save(drone));
    }

    @Transactional
    public DroneResponse updateDroneStatus(
            Long id,
            DroneStatusRequest request
    ) {

        Drone drone = droneRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Drone not found with id: " + id
                        )
                );

        drone.setStatus(request.getStatus());

        return toResponse(droneRepository.save(drone));
    }

    @Transactional
    public void deleteDrone(Long id) {

        Drone drone = droneRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Drone not found with id: " + id
                        )
                );

        droneRepository.delete(drone);
    }

    private DroneResponse toResponse(Drone drone) {

        DroneResponse response = new DroneResponse();

        response.setId(drone.getId());
        response.setName(drone.getName());
        response.setSerialNumber(drone.getSerialNumber());
        response.setModel(drone.getModel());
        response.setManufacturer(drone.getManufacturer());
        response.setDroneType(drone.getDroneType());
        response.setStatus(drone.getStatus());
        response.setPurchaseDate(drone.getPurchaseDate());
        response.setCreatedAt(drone.getCreatedAt());
        response.setUpdatedAt(drone.getUpdatedAt());

        return response;
    }
}