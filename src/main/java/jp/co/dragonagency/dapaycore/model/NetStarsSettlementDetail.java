package jp.co.dragonagency.dapaycore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * ネットスターズ還元データ（取引明細）を表すエンティティ。
 * m_netstars_settlement_detail テーブルの 1 行に対応し、
 * StarPay 還元データ項目仕様書の CSV 1 行を保持する。
 */
@Entity
@Table(name = "m_netstars_settlement_detail")
public class NetStarsSettlementDetail {

    /** 取引種類：支払。 */
    public static final String TRADE_TYPE_PAY = "PAY";

    /** 取引種類：返金。 */
    public static final String TRADE_TYPE_REFUND = "REFUND";

    /** 取引種類：取消。 */
    public static final String TRADE_TYPE_REVOKED = "REVOKED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "dedup_key")
    private String dedupKey;

    @Column(name = "shop_code")
    private String shopCode;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "trade_time_raw")
    private String tradeTimeRaw;

    @Column(name = "trade_time")
    private LocalDateTime tradeTime;

    @Column(name = "mch_trade_no")
    private String mchTradeNo;

    @Column(name = "dev_trade_no")
    private String devTradeNo;

    @Column(name = "trade_type")
    private String tradeType;

    @Column(name = "pay_type")
    private String payType;

    @Column(name = "amount")
    private long amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "device_no")
    private String deviceNo;

    @Column(name = "out_trade_no")
    private String outTradeNo;

    @Column(name = "detail")
    private String detail;

    @Column(name = "attach")
    private String attach;

    @Column(name = "import_history_id")
    private Long importHistoryId;

    @Column(name = "first_imported_at")
    private LocalDateTime firstImportedAt;

    @Column(name = "last_imported_at")
    private LocalDateTime lastImportedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public void setDedupKey(String dedupKey) {
        this.dedupKey = dedupKey;
    }

    public String getShopCode() {
        return shopCode;
    }

    public void setShopCode(String shopCode) {
        this.shopCode = shopCode;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getTradeTimeRaw() {
        return tradeTimeRaw;
    }

    public void setTradeTimeRaw(String tradeTimeRaw) {
        this.tradeTimeRaw = tradeTimeRaw;
    }

    public LocalDateTime getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(LocalDateTime tradeTime) {
        this.tradeTime = tradeTime;
    }

    public String getMchTradeNo() {
        return mchTradeNo;
    }

    public void setMchTradeNo(String mchTradeNo) {
        this.mchTradeNo = mchTradeNo;
    }

    public String getDevTradeNo() {
        return devTradeNo;
    }

    public void setDevTradeNo(String devTradeNo) {
        this.devTradeNo = devTradeNo;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public Long getImportHistoryId() {
        return importHistoryId;
    }

    public void setImportHistoryId(Long importHistoryId) {
        this.importHistoryId = importHistoryId;
    }

    public LocalDateTime getFirstImportedAt() {
        return firstImportedAt;
    }

    public void setFirstImportedAt(LocalDateTime firstImportedAt) {
        this.firstImportedAt = firstImportedAt;
    }

    public LocalDateTime getLastImportedAt() {
        return lastImportedAt;
    }

    public void setLastImportedAt(LocalDateTime lastImportedAt) {
        this.lastImportedAt = lastImportedAt;
    }

    @Override
    public String toString() {
        return "NetStarsSettlementDetail{"
                + "id=" + id
                + ", dedupKey=" + dedupKey
                + ", tradeType=" + tradeType
                + ", outTradeNo=" + outTradeNo
                + ", amount=" + amount
                + "}";
    }
}
