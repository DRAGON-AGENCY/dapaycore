package jp.co.dragonagency.dapaycore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import jp.co.dragonagency.dapaycore.dto.MerchantApplicationRequest;
import jp.co.dragonagency.dapaycore.dto.MerchantApplicationResponse;
import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationDocumentRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;

/**
 * MerchantApplicationService の書類保存統合部分の単体テスト（項番 T7〜T10）。
 * 申込内容（各ステップの項目）そのものの検証は対象外とし、
 * DocumentStorageService との連携のみを検証する。
 */
@ExtendWith(MockitoExtension.class)
class MerchantApplicationServiceTest {

    @Mock
    private MerchantApplicationRepository merchantApplicationRepository;

    @Mock
    private MerchantApplicationDocumentRepository documentRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DocumentStorageService documentStorageService;

    private MerchantApplicationService service;

    @BeforeEach
    void setUp() {
        service = new MerchantApplicationService(
                merchantApplicationRepository,
                documentRepository,
                jdbcTemplate,
                passwordEncoder,
                documentStorageService);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any()))
                .thenReturn(1L);
    }

    @Test
    void T7_register_ファイル保存に成功したときfile_pathに保存先の識別子が記録される()
            throws IOException, SQLException {
        MultipartFile idDocFront = new MockMultipartFile(
                "idDocFront", "id.png", "image/png",
                "content".getBytes(StandardCharsets.UTF_8));
        when(documentStorageService.store(
                anyString(), eq(MerchantApplicationDocument.TYPE_ID_FRONT), eq(idDocFront)))
                .thenReturn("MA-2026-00001/ID_FRONT/dummy.png");

        service.register(new MerchantApplicationRequest(),
                null, idDocFront, null, null, null, null);

        ArgumentCaptor<BatchPreparedStatementSetter> captor =
                ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());
        PreparedStatement ps = mock(PreparedStatement.class);
        captor.getValue().setValues(ps, 0);
        verify(ps).setString(4, "MA-2026-00001/ID_FRONT/dummy.png");
    }

    @Test
    void T8_register_ファイル保存が失敗したときfile_pathが空文字で書類登録が継続する()
            throws IOException, SQLException {
        MultipartFile idDocFront = new MockMultipartFile(
                "idDocFront", "id.png", "image/png",
                "content".getBytes(StandardCharsets.UTF_8));
        when(documentStorageService.store(
                anyString(), eq(MerchantApplicationDocument.TYPE_ID_FRONT), eq(idDocFront)))
                .thenThrow(new IOException("disk full"));

        MerchantApplicationResponse response = service.register(
                new MerchantApplicationRequest(),
                null, idDocFront, null, null, null, null);

        assertThat(response.isSuccess()).isTrue();
        ArgumentCaptor<BatchPreparedStatementSetter> captor =
                ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());
        PreparedStatement ps = mock(PreparedStatement.class);
        captor.getValue().setValues(ps, 0);
        verify(ps).setString(4, "");
    }

    @Test
    void T9_register_ファイルが指定されていないときその書類種別は登録されない() {
        service.register(new MerchantApplicationRequest(),
                null, null, null, null, null, null);

        verify(jdbcTemplate, never())
                .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void T10_register_複数ファイルのリストがそれぞれ個別に保存される() throws IOException {
        MultipartFile file1 = new MockMultipartFile(
                "businessPermits", "permit1.pdf", "application/pdf",
                "a".getBytes(StandardCharsets.UTF_8));
        MultipartFile file2 = new MockMultipartFile(
                "businessPermits", "permit2.pdf", "application/pdf",
                "b".getBytes(StandardCharsets.UTF_8));
        when(documentStorageService.store(
                anyString(), eq(MerchantApplicationDocument.TYPE_BUSINESS_PERMIT), any()))
                .thenReturn("dummy/path.pdf");

        service.register(new MerchantApplicationRequest(),
                List.of(file1, file2), null, null, null, null, null);

        verify(documentStorageService, times(2)).store(
                anyString(), eq(MerchantApplicationDocument.TYPE_BUSINESS_PERMIT), any());
    }
}
