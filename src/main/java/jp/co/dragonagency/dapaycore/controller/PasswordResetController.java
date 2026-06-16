package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.PasswordResetRequest;
import jp.co.dragonagency.dapaycore.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * パスワード再設定（仮パスワード再発行）API を提供するコントローラ。
 */
@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * 仮パスワードを再発行してメール送信する。
     * メールアドレスが未登録の場合はエラーメッセージを返す。
     *
     * @param request メールアドレスを含むリクエスト
     * @return 成功時 success:true、未登録時 success:false + errorMessage
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> requestReset(
            @RequestBody PasswordResetRequest request) {
        try {
            boolean found = passwordResetService.requestReset(request.getEmail());
            if (found) {
                return ResponseEntity.ok(Map.of("success", true));
            }
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("errorMessage",
                    "入力されたメールアドレスは登録されていません。");
            return ResponseEntity.badRequest().body(body);
        } catch (Exception e) {
            log.error("パスワード再設定処理エラー: {}", e.getMessage(), e);
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("errorMessage",
                    "処理に失敗しました。時間をおいて再度お試しください。");
            return ResponseEntity.internalServerError().body(body);
        }
    }
}
