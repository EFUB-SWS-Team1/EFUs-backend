package com.efus.backend.domain.receipt.entity;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "receipt",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_receipt_transaction", columnNames = "transaction_id"),
                @UniqueConstraint(name = "uk_receipt_storage_key", columnNames = "storage_key")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receipt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_term_member_id", nullable = false)
    private TermMember uploadedByTermMember;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "presigned_url", columnDefinition = "text")
    private String presignedUrl;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Builder
    public Receipt(
            Transaction transaction,
            TermMember uploadedByTermMember,
            String storageKey,
            String presignedUrl,
            String originalFilename,
            String contentType,
            Long fileSize
    ) {
        this.transaction = transaction;
        this.uploadedByTermMember = uploadedByTermMember;
        this.storageKey = storageKey;
        this.presignedUrl = presignedUrl;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }
    public void replaceFile(
            TermMember uploadedByTermMember,
            String storageKey,
            String presignedUrl,
            String originalFilename,
            String contentType,
            Long fileSize
    ) {
        this.uploadedByTermMember = uploadedByTermMember;
        this.storageKey = storageKey;
        this.presignedUrl = presignedUrl;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }
}
