package com.dmfs.geologist.repository;
import com.dmfs.geologist.entity.ProcessedMagneticData;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedMagneticDataRepository extends JpaRepository<ProcessedMagneticData, Long> {}
