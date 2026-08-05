package jp.co.dragonagency.dapaycore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

/**
 * LocalDocumentStorageService の単体テスト（項番 T1〜T6）。
 */
class LocalDocumentStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void T1_store_ファイルを保存しmemberCodeとdocumentTypeを含む相対パスを返す() throws IOException {
        LocalDocumentStorageService service =
                new LocalDocumentStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file", "営業許可証.pdf", "application/pdf", "content".getBytes(StandardCharsets.UTF_8));

        String storedPath = service.store("MA-2026-00001", "BUSINESS_PERMIT", file);

        assertThat(storedPath).startsWith("MA-2026-00001/BUSINESS_PERMIT/");
        assertThat(storedPath).endsWith("_営業許可証.pdf");
        assertThat(Files.exists(tempDir.resolve(storedPath))).isTrue();
    }

    @Test
    void T2_store_保存したファイルの内容が元のファイルと一致する() throws IOException {
        LocalDocumentStorageService service =
                new LocalDocumentStorageService(tempDir.toString());
        byte[] content = "hello world".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file =
                new MockMultipartFile("file", "test.txt", "text/plain", content);

        String storedPath = service.store("MA-2026-00002", "ID_FRONT", file);

        assertThat(Files.readAllBytes(tempDir.resolve(storedPath))).isEqualTo(content);
    }

    @Test
    void T3_loadAsResource_保存済みファイルを読み込める() throws IOException {
        LocalDocumentStorageService service =
                new LocalDocumentStorageService(tempDir.toString());
        byte[] content = "readable content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file =
                new MockMultipartFile("file", "read.txt", "text/plain", content);
        String storedPath = service.store("MA-2026-00003", "ID_BACK", file);

        Resource resource = service.loadAsResource(storedPath);

        try (InputStream in = resource.getInputStream()) {
            assertThat(in.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    void T4_loadAsResource_存在しないパスのときIOExceptionをスローする() {
        LocalDocumentStorageService service =
                new LocalDocumentStorageService(tempDir.toString());

        assertThatThrownBy(() -> service.loadAsResource("no-such-member/ID_FRONT/missing.pdf"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void T5_loadAsResource_baseDirの外を指すパスのときIOExceptionをスローする() {
        LocalDocumentStorageService service =
                new LocalDocumentStorageService(tempDir.toString());

        assertThatThrownBy(() -> service.loadAsResource("../outside.txt"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void T6_store_ファイル名の危険な文字を置換して保存する() throws IOException {
        LocalDocumentStorageService service =
                new LocalDocumentStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file", "a/b\\c:d.txt", "text/plain",
                "x".getBytes(StandardCharsets.UTF_8));

        String storedPath = service.store("MA-2026-00004", "OPENING_PLAN", file);

        assertThat(storedPath).doesNotContain("a/b\\c:d.txt");
        assertThat(Files.exists(tempDir.resolve(storedPath))).isTrue();
    }
}
