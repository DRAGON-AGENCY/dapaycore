package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.FeeRateRequest;
import jp.co.dragonagency.dapaycore.dto.FeeRateResponse;
import jp.co.dragonagency.dapaycore.service.FeeRateService;
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
 * 手数料レート CRUD API および会員名取得 API を提供するコントローラ。
 * 運用管理者が使用する。認証は OperationAuthInterceptor が担う。
 */
@RestController
public class FeeRateApiController {

    private static final Logger log = LoggerFactory.getLogger(FeeRateApiController.class);

    private final FeeRateService feeRateService;

    public FeeRateApiController(FeeRateService feeRateService) {
        this.feeRateService = feeRateService;
    }

    @GetMapping("/api/member/{memberCode}/name")
    public ResponseEntity<Map<String, Object>> getMemberName(
            @PathVariable String memberCode) {
        String name = feeRateService.findMemberName(memberCode);
        if (name == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("memberName", name));
    }

    @GetMapping("/api/fee/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable long id) {
        try {
            return ResponseEntity.ok(toMap(feeRateService.findById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("手数料レート取得エラー id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "データの取得に失敗しました。"));
        }
    }

    @PostMapping("/api/fee")
    public ResponseEntity<Map<String, Object>> create(@RequestBody FeeRateRequest req) {
        try {
            return ResponseEntity.ok(toMap(feeRateService.create(req)));
        } catch (IllegalArgumentException e) {
            log.warn("手数料レート登録 入力エラー: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("手数料レート登録エラー: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "登録に失敗しました。時間をおいて再度お試しください。"));
        }
    }

    @PutMapping("/api/fee/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable long id, @RequestBody FeeRateRequest req) {
        try {
            return ResponseEntity.ok(toMap(feeRateService.update(id, req)));
        } catch (IllegalArgumentException e) {
            log.warn("手数料レート更新 入力エラー: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("手数料レート更新エラー id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "更新に失敗しました。時間をおいて再度お試しください。"));
        }
    }

    @DeleteMapping("/api/fee/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable long id) {
        try {
            feeRateService.delete(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            log.warn("手数料レート削除 入力エラー: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("手数料レート削除エラー id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "削除に失敗しました。"));
        }
    }

    private static Map<String, Object> toMap(FeeRateResponse r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("memberCode", r.getMemberCode());
        map.put("memberName", r.getMemberName());
        map.put("startDate", r.getStartDate());
        map.put("endDate", r.getEndDate());
        map.put("feeRate", r.getFeeRate());
        return map;
    }
}
