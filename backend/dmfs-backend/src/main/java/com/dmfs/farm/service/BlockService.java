package com.dmfs.farm.service;

import com.dmfs.farm.entity.Block;
import com.dmfs.farm.entity.Farm;
import com.dmfs.farm.repository.BlockRepository;
import com.dmfs.farm.repository.FarmRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

        Block block = new Block();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Block name is required");
        }
        if (areaHectares == null || areaHectares <= 0) {
            throw new IllegalArgumentException("Block area must be greater than zero");
        }
        if (farm.getAreaHectares() != null) {
            double allocated = blockRepository.findByFarmId(farmId).stream()
                    .map(Block::getAreaHectares).filter(java.util.Objects::nonNull)
                    .mapToDouble(Double::doubleValue).sum();
            if (allocated + areaHectares > farm.getAreaHectares()) {
                throw new IllegalArgumentException("Block area exceeds the farm's unallocated area");
            }
        }

        block.setName(name);
        block.setDescription(description);
        block.setAreaHectares(areaHectares);
        block.setCenterLatitude(centerLatitude);
        block.setCenterLongitude(centerLongitude);
        block.setFarm(farm);

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
}
