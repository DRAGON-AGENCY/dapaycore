package jp.co.dragonagency.dapaycore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.dragonagency.dapaycore.dto.CorporateNumberSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class CorporateNumberSearchService {

    // NTA API v4 のみ type=12（JSON）をサポート。
    // 参考記事（https://qiita.com/y-okuda/items/0a372018cbf00d7313b7）は v1 URL だが
    // JSON 取得には v4 エンドポイントを使用する。
    private static final String NTA_BASE_URL
            = "https://api.houjin-bangou.nta.go.jp/4/num";

    private final String applicationId;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CorporateNumberSearchService(
            @Value("${nta.corp.api.id}") String applicationId) {
        this.applicationId = applicationId;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public CorporateNumberSearchResult search(String corporateNumber) throws Exception {
        if (corporateNumber == null || !corporateNumber.matches("[0-9]{13}")) {
            throw new IllegalArgumentException("法人番号は13桁の数字で指定してください。");
        }

        String url = NTA_BASE_URL
                + "?id=" + applicationId
                + "&number=" + corporateNumber
                + "&type=12"
                + "&history=0";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() == 404) {
            // NTA API が 404 を返す主な原因はアプリケーション ID 未登録。
            throw new IllegalStateException(
                    "国税庁API 404: アプリケーションIDが未登録の可能性があります。"
                    + " https://www.houjin-bangou.nta.go.jp/webapi/ で申請し"
                    + " 環境変数 NTA_CORP_API_ID に登録済み ID を設定してください。");
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "国税庁APIの呼び出しに失敗しました: HTTP " + response.statusCode()
                    + " body=" + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        int count = root.path("count").asInt(0);
        if (count == 0) {
            return null;
        }

        JsonNode corp = root.path("corporation").get(0);
        CorporateNumberSearchResult result = new CorporateNumberSearchResult();
        result.setCorporateNumber(corporateNumber);
        result.setName(corp.path("name").asText(null));
        result.setFurigana(nullIfEmpty(corp.path("furigana").asText(null)));
        result.setPostCode(nullIfEmpty(corp.path("postCode").asText(null)));
        result.setPrefectureName(nullIfEmpty(corp.path("prefectureName").asText(null)));
        result.setCityName(nullIfEmpty(corp.path("cityName").asText(null)));
        result.setStreetNumber(nullIfEmpty(corp.path("streetNumber").asText(null)));
        return result;
    }

    private String nullIfEmpty(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}
