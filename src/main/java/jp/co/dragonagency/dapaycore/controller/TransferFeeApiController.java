package jp.co.dragonagency.dapaycore.controller;

import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.dto.TransferFeeRequest;
import jp.co.dragonagency.dapaycore.dto.TransferFeeResponse;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.TransferFeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 振込手数料 CRUD API を提供するコントローラ。
 * 運用管理者が使用する。認証は OperationAuthInterceptor が担う。
 */
@RestController
public class TransferFeeApiController {

    private static final Logger log = LoggerFactory.getLogger(TransferFeeApiController.class);

    private final TransferFeeService transferFeeService;

    public TransferFeeApiController(TransferFeeService transferFeeService) {
        this.transferFeeService = transferFeeService;
    }

    @GetMapping("/api/transfer-fee/{bankCode}")
    public ResponseEntity<Map<String, Object>> getByBankCode(@PathVariable String bankCode) {
        try {
            return ResponseEntity.ok(toMap(transferFeeService.findByBankCode(bankCode)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("振込手数料取得エラー bankCode={}: {}", bankCode, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "データの取得に失敗しました。"));
        }
    }

    @PostMapping("/api/transfer-fee")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody TransferFeeRequest req, HttpSession session) {
        try {
            return ResponseEntity.ok(
                    toMap(transferFeeService.create(req, getLoginUserId(session))));
        } catch (IllegalArgumentException e) {
            log.warn("振込手数料登録 入力エラー: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("振込手数料登録エラー: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "登録に失敗しました。時間をおいて再度お試しください。"));
        }
    }

    @PutMapping("/api/transfer-fee/{bankCode}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String bankCode,
            @RequestBody TransferFeeRequest req,
            HttpSession session) {
        try {
            return ResponseEntity.ok(toMap(
                    transferFeeService.update(bankCode, req, getLoginUserId(session))));
        } catch (IllegalArgumentException e) {
            log.warn("振込手数料更新 入力エラー: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("振込手数料更新エラー bankCode={}: {}", bankCode, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "更新に失敗しました。時間をおいて再度お試しください。"));
        }
    }

    @DeleteMapping("/api/transfer-fee/{bankCode}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String bankCode) {
        try {
            transferFeeService.delete(bankCode);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            log.warn("振込手数料削除 入力エラー: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("振込手数料削除エラー bankCode={}: {}", bankCode, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "削除に失敗しました。"));
        }
    }

    private String getLoginUserId(HttpSession session) {
        Object loginUserId = session.getAttribute(SessionAttributeNames.LOGIN_USER);
        return loginUserId == null ? null : loginUserId.toString();
    }

    private static Map<String, Object> toMap(TransferFeeResponse r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("bankCode", r.getBankCode());
        map.put("transferFee", r.getTransferFee());
        map.put("remarks", r.getRemarks());
        return map;
    }
}
