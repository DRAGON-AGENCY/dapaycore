package jp.co.dragonagency.dapaycore.dto;

/**
 * 手数料一覧画面の 1 行分のデータ転送オブジェクト。
 */
public class FeeRateListItemDto {

    private long id;
    private String memberCode;
    private String corporateNameKana;
    private String startDate;
    private String endDate;
    private String feeRateDisplay;
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getFeeRateDisplay() {
        return feeRateDisplay;
    }

    public void setFeeRateDisplay(String feeRateDisplay) {
        this.feeRateDisplay = feeRateDisplay;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
