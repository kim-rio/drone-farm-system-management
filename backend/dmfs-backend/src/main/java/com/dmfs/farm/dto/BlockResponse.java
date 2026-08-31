package com.dmfs.farm.dto;

import com.dmfs.farm.entity.Block;

import java.time.LocalDateTime;

public record BlockResponse(
        Long id,
        String name,
        String description,
        Double areaHectares,
        Double centerLatitude,
        Double centerLongitude,
        Long farmId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static BlockResponse from(Block block) {

        Long farmId = null;

        if (block.getFarm() != null) {
            farmId = block.getFarm().getId();
        }

        return new BlockResponse(
                block.getId(),
                block.getName(),
                block.getDescription(),
                block.getAreaHectares(),
                block.getCenterLatitude(),
                block.getCenterLongitude(),
                farmId,
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }
}