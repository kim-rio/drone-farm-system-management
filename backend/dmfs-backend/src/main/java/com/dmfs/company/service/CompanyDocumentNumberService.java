package com.dmfs.company.service;

import com.dmfs.company.entity.CompanyDocumentSequence;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.repository.CompanyDocumentSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyDocumentNumberService {

    public static final String SERVICE_REQUEST = "SERVICE_REQUEST";
    public static final String MISSION = "MISSION";

    private final CompanyDocumentSequenceRepository repository;

    public CompanyDocumentNumberService(
            CompanyDocumentSequenceRepository repository
    ) {
        this.repository = repository;
    }

    /**
     * Allocates the next number independently for the supplied company and
     * document type. The row is locked for the duration of the transaction,
     * preventing duplicate numbers when two requests arrive concurrently.
     */
    @Transactional
    public long nextNumber(SubscriberCompany company, String documentType) {
        Long companyId = company.getId();
        if (companyId == null) {
            throw new IllegalStateException("Company ID is required for document numbering");
        }

        repository.ensureSequence(companyId, documentType);

        CompanyDocumentSequence sequence = repository
                .findByCompany_IdAndDocumentType(companyId, documentType)
                .orElseThrow(() -> new IllegalStateException(
                        "Document sequence could not be initialized for company " + companyId
                ));

        long number = sequence.getNextNumber();
        sequence.setNextNumber(number + 1);
        repository.save(sequence);

        return number;
    }

    public String nextServiceRequestNumber(SubscriberCompany company) {
        return String.format("SR-%04d", nextNumber(company, SERVICE_REQUEST));
    }

    public String nextMissionNumber(SubscriberCompany company) {
        return String.format("MIS-%04d", nextNumber(company, MISSION));
    }
}
