package jp.co.dragonagency.dapaycore.controller;

import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationDocumentRepository;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.DocumentStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 加盟店申込書類のダウンロードを提供するコントローラ。
 * 運用管理者は全会員の書類を、加盟店本人はセッションに保持する
 * 自身の会員コードに紐づく書類のみ取得できる。
 */
@RestController
public class MerchantApplicationDocumentController {

    private static final Logger log =
            LoggerFactory.getLogger(MerchantApplicationDocumentController.class);

    private final MerchantApplicationDocumentRepository documentRepository;
    private final DocumentStorageService documentStorageService;

    public MerchantApplicationDocumentController(
            MerchantApplicationDocumentRepository documentRepository,
            DocumentStorageService documentStorageService) {
        this.documentRepository = documentRepository;
        this.documentStorageService = documentStorageService;
    }

    /**
     * 書類ファイルをダウンロードする。
     * 未ログイン、または他会員の書類への不正アクセスは 403 で拒否する。
     * 対象の書類が存在しない、またはファイル本体を取得できない場合は 404 を返す。
     *
     * @param documentId 書類ID
     * @param session    HTTP セッション
     * @return ファイルの内容、または 404 / 403 のレスポンス
     */
    @GetMapping("/merchant/document/download")
    public ResponseEntity<Resource> download(
            @RequestParam Long documentId,
            HttpSession session) {

        Optional<MerchantApplicationDocument> found = documentRepository.findById(documentId);
        if (found.isEmpty() || found.get().isDeleteFlag()) {
            return ResponseEntity.notFound().build();
        }

        MerchantApplicationDocument document = found.get();
        if (!isAuthorized(session, document.getMemberCode())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Resource resource = documentStorageService.loadAsResource(document.getFilePath());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            buildContentDisposition(document.getFileName()))
                    .body(resource);
        } catch (IOException e) {
            log.warn("書類ファイルの取得に失敗しました: documentId={}", documentId, e);
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isAuthorized(HttpSession session, String memberCode) {
        if (session == null) {
            return false;
        }
        if (session.getAttribute(SessionAttributeNames.LOGIN_USER) != null) {
            return true;
        }
        Object merchantMemberCode =
                session.getAttribute(SessionAttributeNames.MERCHANT_MEMBER_CODE);
        return merchantMemberCode != null && merchantMemberCode.equals(memberCode);
    }

    private String buildContentDisposition(String fileName) {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "attachment; filename*=UTF-8''" + encodedFileName;
    }
}
