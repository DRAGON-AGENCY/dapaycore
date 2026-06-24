package jp.co.dragonagency.dapaycore.controller;

import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.dto.LoginResult;
import jp.co.dragonagency.dapaycore.dto.OperationLoginRequest;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 運用管理ログイン API を提供するコントローラ。
 * 社員番号とパスワードで認証し、成功時にセッションへ社員番号と権限コードを保持する。
 */
@RestController
public class OperationLoginController {

    private static final Logger log = LoggerFactory.getLogger(OperationLoginController.class);

    private final EmployeeService employeeService;

    public OperationLoginController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * 運用管理ログインを処理する。
     * 認証成功時にセッションへ社員番号と権限コードを保持する。
     *
     * @param request 社員番号とパスワードを含む要求
     * @param session セッション
     * @return 認証結果
     */
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResult> login(
            @RequestBody OperationLoginRequest request,
            HttpSession session) {
        LoginResult result = employeeService.login(
                request.getEmployeeNumber(), request.getPassword());
        if (result.isSuccess()) {
            session.setAttribute(
                    SessionAttributeNames.LOGIN_USER,
                    result.getEmployeeNumber());
            session.setAttribute(
                    SessionAttributeNames.AUTHORITY_CODE,
                    result.getAuthorityCode());
            log.info("運用管理ログイン成功: employeeNumber={}", result.getEmployeeNumber());
        } else {
            log.warn("運用管理ログイン失敗: employeeNumber={}", request.getEmployeeNumber());
        }
        return ResponseEntity.ok(result);
    }
}
