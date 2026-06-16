package jp.co.dragonagency.dapaycore.dto;

/**
 * パスワード再設定リクエスト DTO。
 */
public class PasswordResetRequest {

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
