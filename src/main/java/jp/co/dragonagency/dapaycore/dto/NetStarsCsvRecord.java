package jp.co.dragonagency.dapaycore.dto;

/**
 * ネットスターズ還元データ CSV の 1 行を表す値オブジェクト。
 * StarPay 還元データ項目仕様書 v1.0.11「３－１. 還元データ項目」に対応する。
 * すべての項目は CSV の原文（前後空白を除去したもの）を保持する。
 *
 * @param shopCode    店舗コード
 * @param shopName    店舗名称
 * @param tradeTime   取引時間（yyyyMMddHHmmss 形式の原文）
 * @param mchTradeNo  サーバー取引番号（取消時は空文字）
 * @param devTradeNo  端末取引番号（取消時は空文字）
 * @param type        取引種類（PAY / REFUND / REVOKED）
 * @param payType     支払種別
 * @param amount      取引金額（円・小数点なし）
 * @param currency    通貨種類（JPY 固定）
 * @param deviceId    端末 ID
 * @param deviceNo    端末番号
 * @param outTradeNo  Mch 取引番号（支払と返金で同一）
 * @param detail      商品詳細
 * @param attach      付加情報（オプション項目。存在しない場合は空文字）
 */
public record NetStarsCsvRecord(
        String shopCode,
        String shopName,
        String tradeTime,
        String mchTradeNo,
        String devTradeNo,
        String type,
        String payType,
        String amount,
        String currency,
        String deviceId,
        String deviceNo,
        String outTradeNo,
        String detail,
        String attach) {
}
