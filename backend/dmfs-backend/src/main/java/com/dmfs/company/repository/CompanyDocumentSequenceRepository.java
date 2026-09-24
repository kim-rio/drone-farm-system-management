package com.dmfs.company.repository;

import com.dmfs.company.entity.CompanyDocumentSequence;
import com.dmfs.company.entity.SubscriberCompany;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyDocumentSequenceRepository
        extends JpaRepository<CompanyDocumentSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CompanyDocumentSequence> findByCompany_IdAndDocumentType(
            Long companyId,
            String documentType
    );

    @Modifying
    @Query(value = """
            INSERT INTO company_document_sequences (company_id, document_type, next_number)
            VALUES (:companyId, :documentType, 1)
            ON CONFLICT (company_id, document_type) DO NOTHING
            """, nativeQuery = true)
    int ensureSequence(
            @Param("companyId") Long companyId,
            @Param("documentType") String documentType
    );
}
