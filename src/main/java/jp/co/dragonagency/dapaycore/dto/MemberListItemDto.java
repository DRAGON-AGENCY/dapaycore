package jp.co.dragonagency.dapaycore.dto;

import java.time.LocalDateTime;

/**
 * 会員一覧画面の 1 行分のデータを保持する DTO。
 * m_merchant_application と m_merchant_application_document を結合して生成する。
 */
public class MemberListItemDto {

    private String memberCode;
    private String corporateName;
    private String repName;
    private String applicationStatus;
    private LocalDateTime submittedAt;
    private long documentCount;

    public MemberListItemDto(
            String memberCode,
            String corporateName,
            String repLastName,
            String repFirstName,
            String applicationStatus,
            LocalDateTime submittedAt,
            long documentCount) {
        this.memberCode = memberCode;
        this.corporateName = corporateName != null ? corporateName : "";
        this.repName = buildRepName(repLastName, repFirstName);
        this.applicationStatus = applicationStatus;
        this.submittedAt = submittedAt;
        this.documentCount = documentCount;
    }

    private static String buildRepName(String lastName, String firstName) {
        StringBuilder sb = new StringBuilder();
        if (lastName != null) {
            sb.append(lastName);
        }
        if (firstName != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(firstName);
        }
        return sb.toString();
    }

    public String getMemberCode() {
        return memberCode;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public String getRepName() {
        return repName;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public long getDocumentCount() {
        return documentCount;
    }

    public String getStatusLabel() {
        if (applicationStatus == null) {
            return "";
        }
        switch (applicationStatus) {
            case "UNREVIEWED": return "未審査";
            case "REVIEWING":  return "審査中";
            case "APPROVED":   return "承認済み";
            case "REJECTED":   return "否決";
            default:           return applicationStatus;
        }
    }

    public String getStatusClass() {
        if (applicationStatus == null) {
            return "badge-unreviewed";
        }
        switch (applicationStatus) {
            case "REVIEWING":  return "badge-reviewing";
            case "APPROVED":   return "badge-approved";
            case "REJECTED":   return "badge-rejected";
            default:           return "badge-unreviewed";
        }
    }
}
