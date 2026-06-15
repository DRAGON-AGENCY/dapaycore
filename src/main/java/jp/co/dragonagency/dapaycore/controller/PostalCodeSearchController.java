package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.PostalCodeSearchResult;
import jp.co.dragonagency.dapaycore.service.PostalCodeSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postal-code")
public class PostalCodeSearchController {

    private final PostalCodeSearchService postalCodeSearchService;

    public PostalCodeSearchController(PostalCodeSearchService postalCodeSearchService) {
        this.postalCodeSearchService = postalCodeSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<PostalCodeSearchResult> search(
            @RequestParam String zip) {
        String normalizedZip = zip.replaceAll("[^0-9]", "");
        if (normalizedZip.length() != 7) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PostalCodeSearchResult result = postalCodeSearchService.search(normalizedZip);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
