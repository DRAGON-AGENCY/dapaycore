package jp.co.dragonagency.dapaycore.dto;

/**
 * ログイン認証の結果を保持するクラス。
 * 認証の成否とメッセージ、成功時は社員番号と権限コードを返す。
 */
public class LoginResult {

    private final boolean success;
    private final String message;
    private final String employeeNumber;
    private final String authorityCode;

    public LoginResult(
            boolean success,
            String message,
            String employeeNumber,
            String authorityCode) {
        this.success = success;
        this.message = message;
        this.employeeNumber = employeeNumber;
        this.authorityCode = authorityCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getAuthorityCode() {
        return authorityCode;
    }
}
