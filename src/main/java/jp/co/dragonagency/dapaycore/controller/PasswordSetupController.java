package jp.co.dragonagency.dapaycore.controller;

import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.dto.PasswordSetupRequest;
import jp.co.dragonagency.dapaycore.dto.PasswordSetupResponse;
import jp.co.dragonagency.dapaycore.dto.PasswordSetupSessionRequest;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.PasswordSetupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 本パスワード登録 API を提供するコントローラ。
 */
@RestController
@RequestMapping("/api/password-setup")
public class PasswordSetupController {

    private static final Logger log = LoggerFactory.getLogger(PasswordSetupController.class);

    private final PasswordSetupService passwordSetupService;

    public PasswordSetupController(PasswordSetupService passwordSetupService) {
        this.passwordSetupService = passwordSetupService;
    }

    /**
     * 本パスワードを登録する（スタンドアロン）。
     * 会員コードと仮パスワードを含むリクエストで照合する。
     */
    @PostMapping
    public ResponseEntity<PasswordSetupResponse> setup(
            @RequestBody PasswordSetupRequest request) {
        try {
            PasswordSetupResponse response = passwordSetupService.setup(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("本パスワード登録エラー: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new PasswordSetupResponse(false,
                            "処理に失敗しました。時間をおいて再度お試しください。"));
        }
    }

    /**
     * セッションのログイン状態を返す。
     * 本パスワード登録画面がセッションモードで動作するかを判定するために使用する。
     */
    @GetMapping("/session-status")
    public ResponseEntity<Map<String, Boolean>> getSessionStatus(HttpSession session) {
        boolean loggedIn =
                session.getAttribute(SessionAttributeNames.MERCHANT_MEMBER_CODE) != null;
        return ResponseEntity.ok(Map.of("loggedIn", loggedIn));
    }

    /**
     * セッション経由で本パスワードを登録する。
     * 仮パスワードログイン後に呼び出す。会員コードはセッションから取得する。
     */
    @PostMapping("/session")
    public ResponseEntity<PasswordSetupResponse> setupFromSession(
            @RequestBody PasswordSetupSessionRequest request,
            HttpSession session) {
        String memberCode = (String) session.getAttribute(
                SessionAttributeNames.MERCHANT_MEMBER_CODE);
        if (memberCode == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PasswordSetupResponse(false, "ログインが必要です。"));
        }
        try {
            PasswordSetupResponse response = passwordSetupService.setupFromSession(
                    memberCode, request.getNewPassword(), request.getConfirmPassword());
            if (response.isSuccess()) {
                session.removeAttribute(SessionAttributeNames.MERCHANT_MEMBER_CODE);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("本パスワード登録エラー（セッション）: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new PasswordSetupResponse(false,
                            "処理に失敗しました。時間をおいて再度お試しください。"));
        }
    }
}
