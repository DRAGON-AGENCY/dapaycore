package jp.co.dragonagency.dapaycore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ネットスターズ StarPay 還元データの CSV リクエスト API を呼び出すクライアント。
 * StarPay 還元データ項目仕様書 v1.0.11「３－２－２. CSV リクエストインターフェース」に従う。
 * エンドポイント:
 * {@code https://{DOMAIN}/api/v1/csvdetails/{entcode}?key=...&begindate=...&enddate=...&pageindex=...&pagesize=...}
 *
 * <p>接続情報（ドメイン・企業コード・key）はネットスターズから個別に受領するため、
 * 未設定の場合は {@link #isConfigured()} が false を返す。</p>
 */
@Component
public class NetStarsReductionDataApiClient {

    private static final Logger log =
            LoggerFactory.getLogger(NetStarsReductionDataApiClient.class);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int PAGE_SIZE_MIN = 1;
    private static final int PAGE_SIZE_MAX = 1000;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final String domain;
    private final String entCode;
    private final String key;
    private final int pageSize;
    private final HttpClient httpClient;

    public NetStarsReductionDataApiClient(
            @Value("${netstars.api.domain:}") String domain,
            @Value("${netstars.api.ent-code:}") String entCode,
            @Value("${netstars.api.key:}") String key,
            @Value("${netstars.api.page-size:1000}") int pageSize) {
        this.domain = trimToEmpty(domain);
        this.entCode = trimToEmpty(entCode);
        this.key = trimToEmpty(key);
        this.pageSize = clampPageSize(pageSize);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    /**
     * API 接続情報（ドメイン・企業コード・key）がすべて設定済みかどうかを返す。
     *
     * @return すべて設定済みの場合は true
     */
    public boolean isConfigured() {
        return !domain.isEmpty() && !entCode.isEmpty() && !key.isEmpty();
    }

    /**
     * 1 ページ分の還元データ CSV を取得する。
     * ページング仕様に従い、pageindex は 1 始まり、pagesize は全ページで同一とする。
     *
     * @param beginDate 取得対象期間の開始日
     * @param endDate 取得対象期間の終了日
     * @param pageIndex 取得するページ番号（1 始まり）
     * @return CSV 本文（1 行目はヘッダ）
     * @throws IOException 通信に失敗した場合、または応答が 200 以外の場合
     * @throws InterruptedException 通信が中断された場合
     */
    public String fetchCsvPage(LocalDate beginDate, LocalDate endDate, int pageIndex)
            throws IOException, InterruptedException {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "ネットスターズ還元データ API の接続情報が未設定です。");
        }
        URI uri = buildUri(beginDate, endDate, pageIndex);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "text/plain")
                .GET()
                .build();
        log.info("還元データAPI 取得: begindate={} enddate={} pageindex={} pagesize={}",
                beginDate, endDate, pageIndex, pageSize);
        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException(
                    "還元データAPI がエラーを返しました: HTTP " + response.statusCode()
                    + " body=" + abbreviate(response.body()));
        }
        return response.body();
    }

    /**
     * 1 ページあたりの取得件数（pagesize）を返す。
     *
     * @return pagesize
     */
    public int getPageSize() {
        return pageSize;
    }

    private URI buildUri(LocalDate beginDate, LocalDate endDate, int pageIndex) {
        String base = domain.endsWith("/")
                ? domain.substring(0, domain.length() - 1)
                : domain;
        String url = base
                + "/api/v1/csvdetails/" + encode(entCode)
                + "?key=" + encode(key)
                + "&begindate=" + DATE_FORMAT.format(beginDate)
                + "&enddate=" + DATE_FORMAT.format(endDate)
                + "&pageindex=" + pageIndex
                + "&pagesize=" + pageSize;
        return URI.create(url);
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static int clampPageSize(int value) {
        if (value < PAGE_SIZE_MIN) {
            return PAGE_SIZE_MIN;
        }
        if (value > PAGE_SIZE_MAX) {
            return PAGE_SIZE_MAX;
        }
        return value;
    }

    private static String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 500 ? value : value.substring(0, 500) + "...";
    }
}
