package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.CorporateNumberSearchResult;
import jp.co.dragonagency.dapaycore.service.CorporateNumberSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/corporate")
public class CorporateNumberSearchController {

    private static final Logger log
            = LoggerFactory.getLogger(CorporateNumberSearchController.class);

    private final CorporateNumberSearchService corporateNumberSearchService;

    public CorporateNumberSearchController(
            CorporateNumberSearchService corporateNumberSearchService) {
        this.corporateNumberSearchService = corporateNumberSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<CorporateNumberSearchResult> search(
            @RequestParam String number) {
        String normalized = number.replaceAll("[^0-9]", "");
        if (normalized.length() != 13) {
            return ResponseEntity.badRequest().build();
        }

        try {
            CorporateNumberSearchResult result =
                    corporateNumberSearchService.search(normalized);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("国税庁API設定エラー: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            log.error("法人番号検索エラー number={}: {}", normalized, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
