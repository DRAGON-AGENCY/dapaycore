package jp.co.dragonagency.dapaycore.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * お問い合わせを表すエンティティ。
 * m_contact_inquiry テーブルの 1 行に対応し、お問い合わせ管理で使用する。
 */
@Entity
@Table(name = "m_contact_inquiry")
public class ContactInquiry {

    /** ステータス：受付中 */
    public static final String STATUS_RECEIVED = "RECEIVED";

    /** ステータス：対応中 */
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";

    /** ステータス：回答済み */
    public static final String STATUS_ANSWERED = "ANSWERED";

    /** ステータス：クローズ */
    public static final String STATUS_CLOSED = "CLOSED";

    @Id
    @Column(name = "inquiry_number")
    private String inquiryNumber;

    @Column(name = "category")
    private String category;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getInquiryNumber() {
        return inquiryNumber;
    }

    public void setInquiryNumber(String inquiryNumber) {
        this.inquiryNumber = inquiryNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ContactInquiry{"
                + "inquiryNumber=" + inquiryNumber
                + ", category=" + category
                + ", subject=" + subject
                + ", status=" + status
                + "}";
    }
}
