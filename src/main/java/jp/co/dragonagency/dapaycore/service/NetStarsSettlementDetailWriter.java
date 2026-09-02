package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.NetStarsCsvRecord;
import jp.co.dragonagency.dapaycore.model.NetStarsSettlementDetail;
import jp.co.dragonagency.dapaycore.repository.NetStarsSettlementDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * ネットスターズ還元データ明細を 1 ページ単位でトランザクション内に永続化するサービス。
 * 取込処理本体（HTTP 通信を含む）を長いトランザクションにしないため、
 * ページ単位の書き込みをこのクラスに分離している。
 * 重複排除キーが一致する既存レコードは上書き（UPSERT）する。
 */
@Service
public class NetStarsSettlementDetailWriter {

    private static final Logger log =
            LoggerFactory.getLogger(NetStarsSettlementDetailWriter.class);

    private static final DateTimeFormatter TRADE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern TRADE_TIME_PATTERN = Pattern.compile("\\d{14}");

    private static final int MAX_DEDUP_KEY = 120;
    private static final int MAX_SHOP_CODE = 32;
    private static final int MAX_SHOP_NAME = 128;
    private static final int MAX_TRADE_TIME_RAW = 20;
    private static final int MAX_MCH_TRADE_NO = 32;
    private static final int MAX_DEV_TRADE_NO = 20;
    private static final int MAX_TRADE_TYPE = 8;
    private static final int MAX_PAY_TYPE = 32;
    private static final int MAX_CURRENCY = 3;
    private static final int MAX_DEVICE_ID = 96;
    private static final int MAX_DEVICE_NO = 8;
    private static final int MAX_OUT_TRADE_NO = 32;
    private static final int MAX_DETAIL = 256;
    private static final int MAX_ATTACH = 128;

    private final NetStarsSettlementDetailRepository detailRepository;

    public NetStarsSettlementDetailWriter(
            NetStarsSettlementDetailRepository detailRepository) {
        this.detailRepository = detailRepository;
    }

    /**
     * 1 ページ分の CSV レコードを永続化する。
     *
     * @param records 永続化対象のレコード
     * @param historyId 対応する取込履歴の id
     * @return 新規登録件数と更新件数
     */
    @Transactional
    public PageWriteResult writePage(List<NetStarsCsvRecord> records, long historyId) {
        int inserted = 0;
        int updated = 0;
        for (NetStarsCsvRecord record : records) {
            String dedupKey = buildDedupKey(record);
            Optional<NetStarsSettlementDetail> found =
                    detailRepository.findByDedupKey(dedupKey);
            NetStarsSettlementDetail entity =
                    found.orElseGet(NetStarsSettlementDetail::new);
            boolean isNew = found.isEmpty();

            applyRecord(entity, record, dedupKey);

            LocalDateTime now = LocalDateTime.now();
            if (isNew) {
                entity.setFirstImportedAt(now);
            }
            entity.setLastImportedAt(now);
            entity.setImportHistoryId(historyId);
            detailRepository.save(entity);

            if (isNew) {
                inserted++;
            } else {
                updated++;
            }
        }
        return new PageWriteResult(inserted, updated);
    }

    private void applyRecord(
            NetStarsSettlementDetail entity,
            NetStarsCsvRecord record,
            String dedupKey) {
        entity.setDedupKey(dedupKey);
        entity.setShopCode(truncate(record.shopCode(), MAX_SHOP_CODE));
        entity.setShopName(truncate(record.shopName(), MAX_SHOP_NAME));
        entity.setTradeTimeRaw(truncate(record.tradeTime(), MAX_TRADE_TIME_RAW));
        entity.setTradeTime(parseTradeTime(record.tradeTime()));
        entity.setMchTradeNo(truncate(record.mchTradeNo(), MAX_MCH_TRADE_NO));
        entity.setDevTradeNo(truncate(record.devTradeNo(), MAX_DEV_TRADE_NO));
        entity.setTradeType(truncate(record.type(), MAX_TRADE_TYPE));
        entity.setPayType(truncate(record.payType(), MAX_PAY_TYPE));
        entity.setAmount(parseAmount(record.amount()));
        entity.setCurrency(truncate(defaultIfBlank(record.currency(), "JPY"), MAX_CURRENCY));
        entity.setDeviceId(truncate(record.deviceId(), MAX_DEVICE_ID));
        entity.setDeviceNo(truncate(record.deviceNo(), MAX_DEVICE_NO));
        entity.setOutTradeNo(truncate(record.outTradeNo(), MAX_OUT_TRADE_NO));
        entity.setDetail(truncate(record.detail(), MAX_DETAIL));
        entity.setAttach(truncate(record.attach(), MAX_ATTACH));
    }

    /**
     * 重複排除キーを組み立てる。
     * サーバー取引番号が設定されていればそれを使用し、
     * 取消などで空の場合は Mch 取引番号・取引種類・取引時間の組み合わせを使用する。
     *
     * @param record CSV レコード
     * @return 重複排除キー
     */
    String buildDedupKey(NetStarsCsvRecord record) {
        String mchTradeNo = safe(record.mchTradeNo());
        if (!mchTradeNo.isEmpty()) {
            return truncate(mchTradeNo, MAX_DEDUP_KEY);
        }
        String composed = safe(record.outTradeNo())
                + "|" + safe(record.type())
                + "|" + safe(record.tradeTime());
        return truncate(composed, MAX_DEDUP_KEY);
    }

    private LocalDateTime parseTradeTime(String raw) {
        String value = safe(raw);
        if (!TRADE_TIME_PATTERN.matcher(value).matches()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, TRADE_TIME_FORMAT);
        } catch (RuntimeException e) {
            log.warn("還元データCSV: 取引時間 '{}' を解釈できませんでした。", value);
            return null;
        }
    }

    private long parseAmount(String raw) {
        String value = safe(raw);
        if (value.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("還元データCSV: 取引金額 '{}' を数値に変換できませんでした。", value);
            return 0L;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String defaultIfBlank(String value, String fallback) {
        String safe = safe(value);
        return safe.isEmpty() ? fallback : safe;
    }

    private static String truncate(String value, int maxLength) {
        String safe = safe(value);
        return safe.length() <= maxLength ? safe : safe.substring(0, maxLength);
    }

    /**
     * 1 ページ分の書き込み結果。
     *
     * @param inserted 新規登録件数
     * @param updated 更新件数
     */
    public record PageWriteResult(int inserted, int updated) {
    }
}
