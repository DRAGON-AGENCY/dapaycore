package jp.co.dragonagency.dapaycore.dto;

/**
 * 運用管理ログイン要求のリクエストボディを保持するクラス。
 */
public class OperationLoginRequest {

    private String employeeNumber;
    private String password;

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
