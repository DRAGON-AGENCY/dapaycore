package jp.co.dragonagency.dapaycore.dto;

/**
 * セッション経由の本パスワード登録リクエスト。
 * ログイン済みの加盟店が新しいパスワードだけを送信する。
 */
public class PasswordSetupSessionRequest {

    private String newPassword;
    private String confirmPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
