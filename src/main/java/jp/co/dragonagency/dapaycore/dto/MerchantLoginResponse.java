package jp.co.dragonagency.dapaycore.dto;

/**
 * 加盟店ログインレスポンス。
 * 認証の成否と、仮パスワードログインかどうかを返す。
 */
public class MerchantLoginResponse {

    private final boolean success;
    private final String errorMessage;
    private final boolean requiresPasswordSetup;
    private final String memberCode;

    public MerchantLoginResponse(
            boolean success,
            String errorMessage,
            boolean requiresPasswordSetup,
            String memberCode) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.requiresPasswordSetup = requiresPasswordSetup;
        this.memberCode = memberCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isRequiresPasswordSetup() {
        return requiresPasswordSetup;
    }

    public String getMemberCode() {
        return memberCode;
    }
}
