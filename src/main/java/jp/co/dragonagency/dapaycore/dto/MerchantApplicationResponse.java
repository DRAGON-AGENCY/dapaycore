package jp.co.dragonagency.dapaycore.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 加盟店申込登録の処理結果を返すレスポンス DTO。
 */
public class MerchantApplicationResponse {

    private boolean success;
    private String memberCode;
    private String tempPassword;
    private String errorMessage;

    public MerchantApplicationResponse(
            boolean success, String memberCode, String tempPassword, String errorMessage) {
        this.success = success;
        this.memberCode = memberCode;
        this.tempPassword = tempPassword;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    @JsonIgnore
    public String getTempPassword() {
        return tempPassword;
    }

    public void setTempPassword(String tempPassword) {
        this.tempPassword = tempPassword;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
