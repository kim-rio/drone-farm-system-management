package com.dmfs.drone.repository;

import com.dmfs.drone.entity.Drone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DroneRepository extends JpaRepository<Drone, Long> {

    Optional<Drone> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);
}