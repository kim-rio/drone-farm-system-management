package com.dmfs.farm.repository;

import com.dmfs.farm.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {

    List<Block> findByFarmId(Long farmId);
}