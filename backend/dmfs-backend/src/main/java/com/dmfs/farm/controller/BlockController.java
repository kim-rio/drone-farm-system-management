package com.dmfs.farm.controller;

import com.dmfs.farm.dto.BlockResponse;
import com.dmfs.farm.entity.Block;
import com.dmfs.farm.service.BlockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<BlockResponse> createBlock(
            @PathVariable Long farmId,
            @RequestBody BlockRequest request
    ) {

        Block block = blockService.createBlock(
                farmId,
                request.name(),
                request.description(),
                request.areaHectares(),
                request.centerLatitude(),
                request.centerLongitude()
        );

        return ResponseEntity.ok(
                BlockResponse.from(block)
        );
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<List<BlockResponse>> getFarmBlocks(
            @PathVariable Long farmId
    ) {

        List<BlockResponse> blocks =
                blockService.getFarmBlocks(farmId)
                        .stream()
                        .map(BlockResponse::from)
                        .toList();

        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<BlockResponse> getBlock(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                BlockResponse.from(
                        blockService.getBlock(id)
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGEMENT')")
    public ResponseEntity<Void> deleteBlock(
            @PathVariable Long id
    ) {

        blockService.deleteBlock(id);

        return ResponseEntity.noContent().build();
    }

    public record BlockRequest(
            String name,
            String description,
            Double areaHectares,
            Double centerLatitude,
            Double centerLongitude
    ) {
    }
}