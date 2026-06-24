package jp.co.dragonagency.dapaycore.dto;

import java.time.LocalDateTime;

/**
 * 運用管理のお問い合わせ履歴一覧の 1 行分のデータを保持するクラス。
 * m_contact_inquiry と m_merchant_application を結合した情報を持つ。
 */
public class ContactInquiryListItemDto {

    private String inquiryNumber;
    private String memberCode;
    private String corporateNameKana;
    private String category;
    private String subject;
    private String body;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getInquiryNumber() {
        return inquiryNumber;
    }

    public void setInquiryNumber(String inquiryNumber) {
        this.inquiryNumber = inquiryNumber;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getCorporateNameKana() {
        return corporateNameKana;
    }

    public void setCorporateNameKana(String corporateNameKana) {
        this.corporateNameKana = corporateNameKana;
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
}
