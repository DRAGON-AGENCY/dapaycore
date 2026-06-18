package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.MerchantApplicationRequest;
import jp.co.dragonagency.dapaycore.dto.MerchantApplicationResponse;
import jp.co.dragonagency.dapaycore.service.MailService;
import jp.co.dragonagency.dapaycore.service.MerchantApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 加盟店申込の登録 API を提供するコントローラ。
 */
@RestController
@RequestMapping("/api/merchant-application")
public class MerchantApplicationController {

    private static final Logger log =
            LoggerFactory.getLogger(MerchantApplicationController.class);

    private static final int MAX_MAIL_ATTEMPTS = 3;
    /** 試行ごとの待機ミリ秒（初回は即時、2回目は30秒後、3回目は60秒後）。 */
    private static final long[] MAIL_RETRY_DELAY_MS = {0L, 30_000L, 60_000L};

    private final MerchantApplicationService merchantApplicationService;
    private final MailService mailService;
    private final JdbcTemplate jdbcTemplate;

    public MerchantApplicationController(
            MerchantApplicationService merchantApplicationService,
            MailService mailService,
            JdbcTemplate jdbcTemplate) {
        this.merchantApplicationService = merchantApplicationService;
        this.mailService = mailService;
        this.jdbcTemplate = jdbcTemplate;
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
                sendRegistrationMailSilently(request, response);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalArgumentException e) {
            log.warn("加盟店申込バリデーションエラー: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MerchantApplicationResponse(
                            false, null, null, e.getMessage()));

        } catch (Exception e) {
            log.error("加盟店申込登録エラー: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MerchantApplicationResponse(
                            false, null, null, "登録に失敗しました。時間をおいて再度お試しください。"));
        }
    }

    private void sendRegistrationMailSilently(
            MerchantApplicationRequest request,
            MerchantApplicationResponse response) {
        String to = request.getContactEmail();
        if (to == null || to.trim().isEmpty()) {
            log.warn("登録完了メール送信スキップ: 連絡先メールアドレスが未設定 memberCode={}",
                    response.getMemberCode());
            return;
        }
        String memberCode = response.getMemberCode();
        String tempPassword = response.getTempPassword();
        String corporateName = request.getCorporateName();
        String toAddress = to.trim();
        CompletableFuture.runAsync(() -> {
            String lastError = null;
            for (int attempt = 0; attempt < MAX_MAIL_ATTEMPTS; attempt++) {
                if (attempt > 0) {
                    try {
                        Thread.sleep(MAIL_RETRY_DELAY_MS[attempt]);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("登録完了メール送信スレッド割り込み: memberCode={}", memberCode);
                        break;
                    }
                }
                try {
                    mailService.sendRegistrationMail(
                            toAddress, corporateName, memberCode, tempPassword);
                    if (attempt > 0) {
                        log.info("登録完了メール再送信成功: memberCode={} attempt={}",
                                memberCode, attempt + 1);
                    }
                    return;
                } catch (Throwable t) {
                    lastError = t.getMessage();
                    log.warn("登録完了メール送信失敗 ({}/{}): memberCode={} error={}",
                            attempt + 1, MAX_MAIL_ATTEMPTS, memberCode, lastError);
                }
            }
            log.error("登録完了メール送信を全試行で失敗: memberCode={} toAddress={}",
                    memberCode, toAddress);
            recordMailFailure(memberCode, toAddress, corporateName, MAX_MAIL_ATTEMPTS, lastError);
        });
    }

    private void recordMailFailure(
            String memberCode,
            String toAddress,
            String corporateName,
            int retryCount,
            String lastError) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO m_mail_failure "
                    + "(member_code, to_address, corporate_name, retry_count, last_error, failed_at) "
                    + "VALUES (?, ?, ?, ?, ?, NOW())",
                    memberCode, toAddress, corporateName, retryCount, lastError);
        } catch (Exception ex) {
            log.error("メール失敗記録の保存に失敗: memberCode={}", memberCode, ex);
        }
    }
}
