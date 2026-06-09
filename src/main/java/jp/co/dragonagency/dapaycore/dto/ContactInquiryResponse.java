package jp.co.dragonagency.dapaycore.dto;

/**
 * お問い合わせ送信結果を画面へ返すためのクラス。
 * 処理の成否と、失敗時に表示するメッセージを保持する。
 */
public class ContactInquiryResponse {

    private final boolean success;
    private final String message;

    public ContactInquiryResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
