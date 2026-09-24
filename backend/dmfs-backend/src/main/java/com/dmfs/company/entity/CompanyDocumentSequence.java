package com.dmfs.company.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "company_document_sequences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_company_document_sequence",
                columnNames = {"company_id", "document_type"}
        ),
        indexes = @Index(name = "idx_company_document_sequence_company", columnList = "company_id")
)
public class CompanyDocumentSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private SubscriberCompany company;

    @Column(name = "document_type", nullable = false, length = 40)
    private String documentType;

    @Column(name = "next_number", nullable = false)
    private Long nextNumber = 1L;

    public Long getId() {
        return id;
    }

    public SubscriberCompany getCompany() {
        return company;
    }

    public void setCompany(SubscriberCompany company) {
        this.company = company;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Long getNextNumber() {
        return nextNumber;
    }

    public void setNextNumber(Long nextNumber) {
        this.nextNumber = nextNumber;
    }
}
