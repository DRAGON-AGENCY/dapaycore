package jp.co.dragonagency.dapaycore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationDocumentRepository;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.DocumentStorageService;

/**
 * MerchantApplicationDocumentController の単体テスト（項番 T11〜T17）。
 * 書類ダウンロードの認可ロジック（運用管理者は全件、加盟店は自身の書類のみ）を検証する。
 */
@ExtendWith(MockitoExtension.class)
class MerchantApplicationDocumentControllerTest {

    private static final Long DOCUMENT_ID = 1L;
    private static final String OWNER_MEMBER_CODE = "MA-2026-00001";
    private static final String OTHER_MEMBER_CODE = "MA-2026-00002";

    @Mock
    private MerchantApplicationDocumentRepository documentRepository;

    @Mock
    private DocumentStorageService documentStorageService;

    @InjectMocks
    private MerchantApplicationDocumentController controller;

    @Test
    void T11_download_書類が存在しないとき404を返す() {
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        ResponseEntity<Resource> result =
                controller.download(DOCUMENT_ID, new MockHttpSession());

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void T12_download_書類が論理削除済みのとき404を返す() {
        MerchantApplicationDocument doc = buildDocument(OWNER_MEMBER_CODE);
        doc.setDeleteFlag(true);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));

        ResponseEntity<Resource> result =
                controller.download(DOCUMENT_ID, new MockHttpSession());

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void T13_download_未ログインのとき403を返す() {
        when(documentRepository.findById(DOCUMENT_ID))
                .thenReturn(Optional.of(buildDocument(OWNER_MEMBER_CODE)));

        ResponseEntity<Resource> result =
                controller.download(DOCUMENT_ID, new MockHttpSession());

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    void T14_download_他会員としてログインしている加盟店のとき403を返す() {
        when(documentRepository.findById(DOCUMENT_ID))
                .thenReturn(Optional.of(buildDocument(OWNER_MEMBER_CODE)));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributeNames.MERCHANT_MEMBER_CODE, OTHER_MEMBER_CODE);

        ResponseEntity<Resource> result = controller.download(DOCUMENT_ID, session);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    void T15_download_自身の書類にアクセスした加盟店のとき200でファイルを返す() throws IOException {
        MerchantApplicationDocument doc = buildDocument(OWNER_MEMBER_CODE);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));
        when(documentStorageService.loadAsResource(doc.getFilePath()))
                .thenReturn(new ByteArrayResource("content".getBytes()));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributeNames.MERCHANT_MEMBER_CODE, OWNER_MEMBER_CODE);

        ResponseEntity<Resource> result = controller.download(DOCUMENT_ID, session);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void T16_download_運用管理者ログインのとき他会員の書類でも200を返す() throws IOException {
        MerchantApplicationDocument doc = buildDocument(OWNER_MEMBER_CODE);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));
        when(documentStorageService.loadAsResource(anyString()))
                .thenReturn(new ByteArrayResource("content".getBytes()));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributeNames.LOGIN_USER, "user001");

        ResponseEntity<Resource> result = controller.download(DOCUMENT_ID, session);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void T17_download_ストレージからの取得に失敗したとき404を返す() throws IOException {
        MerchantApplicationDocument doc = buildDocument(OWNER_MEMBER_CODE);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));
        when(documentStorageService.loadAsResource(anyString()))
                .thenThrow(new IOException("ファイルが見つかりません"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributeNames.LOGIN_USER, "user001");

        ResponseEntity<Resource> result = controller.download(DOCUMENT_ID, session);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    private MerchantApplicationDocument buildDocument(String memberCode) {
        MerchantApplicationDocument doc = new MerchantApplicationDocument();
        doc.setDocumentId(DOCUMENT_ID);
        doc.setMemberCode(memberCode);
        doc.setDocumentType(MerchantApplicationDocument.TYPE_ID_FRONT);
        doc.setFileName("身分証.pdf");
        doc.setFilePath(memberCode + "/ID_FRONT/20260805_test.pdf");
        return doc;
    }
}
