package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.MerchantApplicationRequest;
import jp.co.dragonagency.dapaycore.dto.MerchantApplicationResponse;
import jp.co.dragonagency.dapaycore.service.MerchantApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 加盟店申込の登録 API を提供するコントローラ。
 */
@RestController
@RequestMapping("/api/merchant-application")
public class MerchantApplicationController {

    private static final Logger log =
            LoggerFactory.getLogger(MerchantApplicationController.class);

    private final MerchantApplicationService merchantApplicationService;

    public MerchantApplicationController(
            MerchantApplicationService merchantApplicationService) {
        this.merchantApplicationService = merchantApplicationService;
    }

    /**
     * 加盟店申込データとファイルをまとめて受け取り、DB に登録する。
     * リクエストは multipart/form-data。"data" パートに JSON を含める。
     */
    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MerchantApplicationResponse> register(
            @RequestPart("data") MerchantApplicationRequest request,
            @RequestParam(value = "fileBusinessPermit", required = false)
                List<MultipartFile> fileBusinessPermit,
            @RequestParam(value = "idDocFront", required = false)
                MultipartFile idDocFront,
            @RequestParam(value = "idDocBack", required = false)
                MultipartFile idDocBack,
            @RequestParam(value = "fileOpeningPlan", required = false)
                List<MultipartFile> fileOpeningPlan,
            @RequestParam(value = "fileProductMaterial", required = false)
                List<MultipartFile> fileProductMaterial,
            @RequestParam(value = "fileEventVenue", required = false)
                List<MultipartFile> fileEventVenue) {

        try {
            MerchantApplicationResponse response = merchantApplicationService.register(
                    request,
                    fileBusinessPermit,
                    idDocFront,
                    idDocBack,
                    fileOpeningPlan,
                    fileProductMaterial,
                    fileEventVenue);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("加盟店申込登録エラー: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MerchantApplicationResponse(
                            false, null, null, "登録に失敗しました。時間をおいて再度お試しください。"));
        }
    }
}
