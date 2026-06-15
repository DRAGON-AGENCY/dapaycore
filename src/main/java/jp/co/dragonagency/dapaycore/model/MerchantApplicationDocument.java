package jp.co.dragonagency.dapaycore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 加盟店申込に添付された書類を表すエンティティ。
 * m_merchant_application_document テーブルの 1 行に対応する。
 */
@Entity
@Table(name = "m_merchant_application_document")
public class MerchantApplicationDocument {

    public static final String TYPE_BUSINESS_PERMIT = "BUSINESS_PERMIT";
    public static final String TYPE_ID_FRONT = "ID_FRONT";
    public static final String TYPE_ID_BACK = "ID_BACK";
    public static final String TYPE_OPENING_PLAN = "OPENING_PLAN";
    public static final String TYPE_PRODUCT_MATERIAL = "PRODUCT_MATERIAL";
    public static final String TYPE_EVENT_VENUE = "EVENT_VENUE";
    public static final String TYPE_STORE_PHOTO = "STORE_PHOTO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private long documentId;

    @Column(name = "member_code")
    private String memberCode;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
