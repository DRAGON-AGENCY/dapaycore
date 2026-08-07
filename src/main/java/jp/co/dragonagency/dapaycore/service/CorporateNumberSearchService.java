package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.CorporateNumberSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class CorporateNumberSearchService {

    // NTA API v4 は JSON 形式に対応していない。type=01/02 は CSV、type=12 が XML。
    // レスポンスは XML（<corporations><corporation>...）で返るため DOM でパースする。
    private static final String NTA_BASE_URL
            = "https://api.houjin-bangou.nta.go.jp/4/num";

    private final String applicationId;
    private final HttpClient httpClient;

    public CorporateNumberSearchService(
            @Value("${nta.corp.api.id}") String applicationId) {
        this.applicationId = applicationId;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
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
                .header("Accept", "application/xml")
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

        Document doc = parseXml(response.body());

        int count = Integer.parseInt(textOf(doc.getDocumentElement(), "count", "0"));
        if (count == 0) {
            return null;
        }

        NodeList corporations = doc.getElementsByTagName("corporation");
        if (corporations.getLength() == 0) {
            return null;
        }
        Element corp = (Element) corporations.item(0);

        CorporateNumberSearchResult result = new CorporateNumberSearchResult();
        result.setCorporateNumber(corporateNumber);
        result.setName(nullIfEmpty(textOf(corp, "name", null)));
        // NTA の法人番号API はフリガナ（読み仮名）情報を提供していないため常に null。
        result.setFurigana(null);
        result.setPostCode(nullIfEmpty(textOf(corp, "postCode", null)));
        result.setPrefectureName(nullIfEmpty(textOf(corp, "prefectureName", null)));
        result.setCityName(nullIfEmpty(textOf(corp, "cityName", null)));
        result.setStreetNumber(nullIfEmpty(textOf(corp, "streetNumber", null)));
        return result;
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // XXE 対策: 外部エンティティ・DOCTYPE の解決を無効化する。
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private String textOf(Element parent, String tagName, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return defaultValue;
        }
        String text = nodes.item(0).getTextContent();
        return text == null ? defaultValue : text;
    }

    private String nullIfEmpty(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}
