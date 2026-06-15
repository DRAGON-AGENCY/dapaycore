package jp.co.dragonagency.dapaycore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.dragonagency.dapaycore.dto.PostalCodeSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Service
public class PostalCodeSearchService {

    private static final Duration TOKEN_MARGIN = Duration.ofSeconds(60);

    private final String baseUrl;
    private final String clientId;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String cachedToken;
    private Instant tokenExpiry;

    public PostalCodeSearchService(
            @Value("${postal.code.api.base-url}") String baseUrl,
            @Value("${postal.code.api.client-id}") String clientId) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public PostalCodeSearchResult search(String zipCode) throws Exception {
        if (zipCode == null || !zipCode.matches("[0-9]{7}")) {
            throw new IllegalArgumentException("郵便番号は7桁の数字で指定してください。");
        }
        String token = acquireToken();
        return callSearchCode(token, zipCode);
    }

    private synchronized String acquireToken() throws Exception {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        String formBody = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&scope=searchcode";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/token"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new RuntimeException("トークン取得に失敗しました: HTTP " + response.statusCode());
        }

        JsonNode json = objectMapper.readTree(response.body());
        String token = json.path("access_token").asText(null);
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("access_token がレスポンスに含まれていません。");
        }
        long expiresIn = json.path("expires_in").asLong(3_600);
        cachedToken = token;
        tokenExpiry = Instant.now().plusSeconds(expiresIn).minus(TOKEN_MARGIN);
        return cachedToken;
    }

    private PostalCodeSearchResult callSearchCode(String token, String zipCode) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/searchcode?code=" + zipCode))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() == 404) {
            return null;
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("住所検索に失敗しました: HTTP " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());

        // API レスポンス構造に合わせてフィールド名を調整すること。
        // 配列形式の場合は先頭要素を使用する。
        JsonNode address;
        if (root.isArray() && root.size() > 0) {
            address = root.get(0);
        } else if (root.has("results") && root.path("results").isArray()
                && root.path("results").size() > 0) {
            address = root.path("results").get(0);
        } else if (root.isObject()) {
            address = root;
        } else {
            return null;
        }

        PostalCodeSearchResult result = new PostalCodeSearchResult();
        result.setZipCode(zipCode);
        result.setPrefecture(firstNonEmpty(address, "prefecture", "kenName", "pref"));
        result.setPrefectureKana(firstNonEmpty(address, "prefectureKana", "kenKana", "prefKana"));
        result.setCity(firstNonEmpty(address, "city", "cityName", "city1"));
        result.setCityKana(firstNonEmpty(address, "cityKana", "cityKana1", "cityKana"));
        result.setTown(firstNonEmpty(address, "town", "townName", "town1"));
        result.setTownKana(firstNonEmpty(address, "townKana", "townKana1"));
        return result;
    }

    private String firstNonEmpty(JsonNode node, String... keys) {
        for (String key : keys) {
            String value = node.path(key).asText(null);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return "";
    }
}
