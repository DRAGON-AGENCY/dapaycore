package jp.co.dragonagency.dapaycore.dto;

/**
 * 本パスワード登録レスポンス。
 */
public class PasswordSetupResponse {

    private final boolean success;
    private final String errorMessage;

    public PasswordSetupResponse(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
