package jp.co.dragonagency.dapaycore.controller;

import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.NetStarsSettlementImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 還元データ受信の停止・再開を制御する API を提供するコントローラ。
 * 運用管理者が還元データ取込画面の STOP / RESTART ボタンから使用する。
 * 認証は OperationAuthInterceptor、CSRF 検査は CsrfProtectionInterceptor が担う。
 */
@RestController
public class NetStarsImportControlApiController {

    private static final Logger log =
            LoggerFactory.getLogger(NetStarsImportControlApiController.class);

    private final NetStarsSettlementImportService importService;

    public NetStarsImportControlApiController(
            NetStarsSettlementImportService importService) {
        this.importService = importService;
    }

    /**
     * 還元データ受信を停止する。
     *
     * @param session HTTP セッション（操作者の社員番号を取得する）
     * @return 処理結果
     */
    @PostMapping("/api/netstars-import/stop")
    public ResponseEntity<Map<String, Object>> stop(HttpSession session) {
        try {
            importService.stopImport(loginUserId(session));
            return ResponseEntity.ok(Map.of(
                    "enabled", false,
                    "message", "還元データ受信を停止しました。"));
        } catch (Exception e) {
            log.error("還元データ受信の停止に失敗しました: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "停止に失敗しました。"));
        }
    }

    /**
     * 還元データ受信を再開し、停止していた期間の取込を開始する。
     *
     * @param session HTTP セッション（操作者の社員番号を取得する）
     * @return 処理結果
     */
    @PostMapping("/api/netstars-import/restart")
    public ResponseEntity<Map<String, Object>> restart(HttpSession session) {
        try {
            importService.restartImport(loginUserId(session));
            importService.triggerRestartImport();
            return ResponseEntity.ok(Map.of(
                    "enabled", true,
                    "message", "還元データ受信を再開しました。"
                            + "停止していた期間の取込を開始しました。"
                            + "しばらくしてから取込履歴をご確認ください。"));
        } catch (Exception e) {
            log.error("還元データ受信の再開に失敗しました: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "再開に失敗しました。"));
        }
    }

    private String loginUserId(HttpSession session) {
        Object loginUserId =
                session.getAttribute(SessionAttributeNames.LOGIN_USER);
        return loginUserId == null ? null : loginUserId.toString();
    }
}
