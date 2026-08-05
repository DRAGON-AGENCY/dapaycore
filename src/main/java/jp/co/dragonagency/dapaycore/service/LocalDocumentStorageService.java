package jp.co.dragonagency.dapaycore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ローカルディスクへ書類ファイルを保存するサービス。
 * 保存先ディレクトリは application.properties の document.storage.base-dir で指定する。
 */
@Service
public class LocalDocumentStorageService implements DocumentStorageService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String UNNAMED_FILE = "unnamed";

    private final Path baseDir;

    public LocalDocumentStorageService(
            @Value("${document.storage.base-dir}") String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(String memberCode, String documentType, MultipartFile file)
            throws IOException {
        String storedFileName = TIMESTAMP_FORMAT.format(LocalDateTime.now())
                + "_" + UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());
        String relativePath = memberCode + "/" + documentType + "/" + storedFileName;

        Path destination = resolveWithinBaseDir(relativePath);
        Files.createDirectories(destination.getParent());
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return relativePath;
    }

    @Override
    public Resource loadAsResource(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IOException("ファイルパスが指定されていません。");
        }

        Path target = resolveWithinBaseDir(filePath);
        Resource resource;
        try {
            resource = new UrlResource(target.toUri());
        } catch (MalformedURLException e) {
            throw new IOException("ファイルの取得に失敗しました。", e);
        }
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("ファイルが見つかりません: " + filePath);
        }
        return resource;
    }

    private Path resolveWithinBaseDir(String relativePath) throws IOException {
        Path resolved = baseDir.resolve(relativePath).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new IOException("不正なファイルパスです: " + relativePath);
        }
        return resolved;
    }

    /**
     * アップロード時のファイル名からディレクトリ部分を取り除き、
     * OS のパスとして不正な文字を置換する。
     * クライアントが送る任意の文字列を Paths.get() に渡すと
     * 環境依存の InvalidPathException が発生し得るため、文字列操作のみで行う。
     */
    private String sanitizeFileName(String originalName) {
        if (originalName == null || originalName.isEmpty()) {
            return UNNAMED_FILE;
        }
        String name = originalName.replace('\\', '/');
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        return name.isEmpty() ? UNNAMED_FILE : name;
    }
}
