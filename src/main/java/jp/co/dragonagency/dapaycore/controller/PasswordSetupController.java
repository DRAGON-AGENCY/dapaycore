package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.PasswordSetupRequest;
import jp.co.dragonagency.dapaycore.dto.PasswordSetupResponse;
import jp.co.dragonagency.dapaycore.service.PasswordSetupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 本パスワードを登録する。
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
}
