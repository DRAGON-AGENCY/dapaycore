package jp.co.dragonagency.dapaycore.dto;

import java.math.BigDecimal;

/**
 * 手数料レート登録・更新リクエスト。
 */
public class FeeRateRequest {

    private String memberCode;
    private String startDate;
    private String endDate;
    private BigDecimal feeRate;

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
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

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }
}
