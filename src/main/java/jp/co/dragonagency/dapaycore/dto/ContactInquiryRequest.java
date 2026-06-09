package jp.co.dragonagency.dapaycore.dto;

/**
 * お問い合わせ送信フォームから受け取るリクエストを保持するクラス。
 */
public class ContactInquiryRequest {

    private String category;
    private String subject;
    private String body;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
