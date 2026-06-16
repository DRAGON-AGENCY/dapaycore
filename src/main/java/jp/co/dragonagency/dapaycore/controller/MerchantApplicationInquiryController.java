package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.service.MerchantApplicationInquiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 申込内容照会画面の更新・削除 API を提供するコントローラ。
 * 運用管理者が使用する。CSRF トークン検査は CsrfProtectionInterceptor が行う。
 */
@RestController
@RequestMapping("/api/merchant-application-inquiry")
public class MerchantApplicationInquiryController {

    private static final Logger log =
            LoggerFactory.getLogger(MerchantApplicationInquiryController.class);

    private final MerchantApplicationInquiryService inquiryService;

    public MerchantApplicationInquiryController(MerchantApplicationInquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    /**
     * 申込情報を更新する。
     * リクエストボディはフィールド名をキー、文字列値をバリューとした Map。
     * boolean はクライアントが "true"/"false" 文字列で送る。
     * 日付は "yyyy-MM-dd"、時刻は "HH:mm" 形式で送る。
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, String> data) {
        try {
            inquiryService.updateApplication(data);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            log.warn("申込情報更新 入力エラー: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("申込情報更新エラー: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "更新に失敗しました。時間をおいて再度お試しください。"));
        }
    }

    /**
     * 申込情報および添付書類を削除する。
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestBody Map<String, String> data) {
        try {
            String memberCode = data.get("memberCode");
            inquiryService.deleteApplication(memberCode);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            log.warn("申込情報削除 入力エラー: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("申込情報削除エラー: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "削除に失敗しました。時間をおいて再度お試しください。"));
        }
    }
}
