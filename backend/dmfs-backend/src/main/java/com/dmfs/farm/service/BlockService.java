package com.dmfs.farm.service;

import com.dmfs.farm.entity.Block;
import com.dmfs.farm.entity.Farm;
import com.dmfs.farm.repository.BlockRepository;
import com.dmfs.farm.repository.FarmRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final FarmRepository farmRepository;

    public BlockService(
            BlockRepository blockRepository,
            FarmRepository farmRepository
    ) {
        this.blockRepository = blockRepository;
        this.farmRepository = farmRepository;
    }

    public Block createBlock(
            Long farmId,
            String name,
            String description,
            Double areaHectares,
            Double centerLatitude,
            Double centerLongitude
    ) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() ->
                        new RuntimeException("Farm not found")
                );

        validateBlock(
                name,
                areaHectares,
                centerLatitude,
                centerLongitude
        );

        validateAvailableFarmArea(
                farm,
                farmId,
                null,
                areaHectares
        );

        Block block = new Block();

        block.setName(name.trim());
        block.setDescription(
                description == null
                        ? null
                        : description.trim()
        );
        block.setAreaHectares(areaHectares);
        block.setCenterLatitude(centerLatitude);
        block.setCenterLongitude(centerLongitude);
        block.setFarm(farm);

        return blockRepository.save(block);
    }

    public Block updateBlock(
            Long id,
            String name,
            String description,
            Double areaHectares,
            Double centerLatitude,
            Double centerLongitude
    ) {
        Block block =
                blockRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Block not found"
                                )
                        );

        validateBlock(
                name,
                areaHectares,
                centerLatitude,
                centerLongitude
        );

        Farm farm = block.getFarm();

        validateAvailableFarmArea(
                farm,
                farm.getId(),
                id,
                areaHectares
        );

        block.setName(name.trim());
        block.setDescription(
                description == null
                        ? null
                        : description.trim()
        );
        block.setAreaHectares(areaHectares);
        block.setCenterLatitude(centerLatitude);
        block.setCenterLongitude(centerLongitude);

        return blockRepository.save(block);
    }

    public List<Block> getFarmBlocks(Long farmId) {
        return blockRepository.findByFarmId(farmId);
    }

    public Block getBlock(Long id) {
        return blockRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Block not found")
                );
    }

    public void deleteBlock(Long id) {
        if (!blockRepository.existsById(id)) {
            throw new RuntimeException("Block not found");
        }

        blockRepository.deleteById(id);
    }

    private void validateBlock(
            String name,
            Double areaHectares,
            Double centerLatitude,
            Double centerLongitude
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Block name is required"
            );
        }

        if (
                areaHectares == null ||
                areaHectares <= 0
        ) {
            throw new IllegalArgumentException(
                    "Block area must be greater than zero"
            );
        }

        if (
                centerLatitude != null &&
                (
                        centerLatitude < -90 ||
                        centerLatitude > 90
                )
        ) {
            throw new IllegalArgumentException(
                    "Block latitude must be between -90 and 90"
            );
        }

        if (
                centerLongitude != null &&
                (
                        centerLongitude < -180 ||
                        centerLongitude > 180
                )
        ) {
            throw new IllegalArgumentException(
                    "Block longitude must be between -180 and 180"
            );
        }
    }

    private void validateAvailableFarmArea(
            Farm farm,
            Long farmId,
            Long excludedBlockId,
            Double requestedArea
    ) {
        if (farm.getAreaHectares() == null) {
            return;
        }

        double allocated =
                blockRepository.findByFarmId(farmId)
                        .stream()
                        .filter(block ->
                                excludedBlockId == null ||
                                !Objects.equals(
                                        block.getId(),
                                        excludedBlockId
                                )
                        )
                        .map(Block::getAreaHectares)
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .sum();

        if (
                requestedArea != null &&
                allocated + requestedArea >
                        farm.getAreaHectares()
        ) {
            throw new IllegalArgumentException(
                    "Block area exceeds the farm's unallocated area"
            );
        }
    }
}
