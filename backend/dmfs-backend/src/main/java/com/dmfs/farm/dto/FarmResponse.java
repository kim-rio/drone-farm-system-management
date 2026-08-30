package com.dmfs.farm.dto;

import com.dmfs.farm.entity.Farm;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

public record FarmResponse(
        Long id,
        String name,
        String description,
        Double latitude,
        Double longitude,
        Double areaHectares,
        Long customerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static FarmResponse from(Farm farm) {

        Point location = farm.getLocation();

        Double latitude = null;
        Double longitude = null;

        if (location != null) {
            longitude = location.getX();
            latitude = location.getY();
        }

        Long customerId = null;

        if (farm.getCustomer() != null) {
            customerId = farm.getCustomer().getId();
        }

        return new FarmResponse(
                farm.getId(),
                farm.getName(),
                farm.getDescription(),
                latitude,
                longitude,
                farm.getAreaHectares(),
                customerId,
                farm.getCreatedAt(),
                farm.getUpdatedAt()
        );
    }
}