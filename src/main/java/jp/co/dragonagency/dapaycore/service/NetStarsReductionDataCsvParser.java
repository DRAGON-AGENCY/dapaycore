package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.NetStarsCsvRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ネットスターズ還元データ CSV を解析するコンポーネント。
 * StarPay 還元データ項目仕様書 v1.0.11「１－１. CSV ファイル仕様」に従う。
 * 区切りは「,」（カンマ）で、値はダブルクオートで囲まれず、
 * すべての値にカンマを含まないため単純な分割で解析できる。
 * 1 行目はヘッダで、2 行目以降が取引データとなる。
 */
@Component
public class NetStarsReductionDataCsvParser {

    private static final Logger log =
            LoggerFactory.getLogger(NetStarsReductionDataCsvParser.class);

    private static final String HEADER_FIRST_COLUMN = "ShopCode";
    private static final char BYTE_ORDER_MARK = '﻿';

    /** 必須項目（店舗コード〜Mch 取引番号）の項目数。 */
    private static final int REQUIRED_FIELD_COUNT = 12;

    /** 項目仕様書で定義された最大項目数（付加情報を含む）。 */
    private static final int MAX_FIELD_COUNT = 14;

    /**
     * CSV 本文を解析して取引明細レコードの一覧を返す。
     * ヘッダ行および項目数が不足した行は読み飛ばす。
     *
     * @param csvBody CSV 本文（UTF-8 でデコード済みの文字列）
     * @return 取引明細レコードの一覧。データが無い場合は空リスト
     */
    public List<NetStarsCsvRecord> parse(String csvBody) {
        List<NetStarsCsvRecord> records = new ArrayList<>();
        if (csvBody == null || csvBody.isBlank()) {
            return records;
        }
        String normalized = stripByteOrderMark(csvBody);
        String[] lines = normalized.split("\\r?\\n", -1);
        int skipped = 0;
        for (String line : lines) {
            if (line.isBlank() || isHeaderLine(line)) {
                continue;
            }
            NetStarsCsvRecord record = parseLine(line);
            if (record == null) {
                skipped++;
                continue;
            }
            records.add(record);
        }
        if (skipped > 0) {
            log.warn("還元データCSV: 項目数不足のため {} 行を読み飛ばしました。", skipped);
        }
        return records;
    }

    private String stripByteOrderMark(String value) {
        if (!value.isEmpty() && value.charAt(0) == BYTE_ORDER_MARK) {
            return value.substring(1);
        }
        return value;
    }

    private boolean isHeaderLine(String line) {
        return line.regionMatches(true, 0, HEADER_FIRST_COLUMN, 0,
                HEADER_FIRST_COLUMN.length())
                && line.toLowerCase().contains("tradetime");
    }

    private NetStarsCsvRecord parseLine(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length < REQUIRED_FIELD_COUNT) {
            log.warn("還元データCSV: 項目数 {} が不足しています。行を読み飛ばします。",
                    fields.length);
            return null;
        }
        return new NetStarsCsvRecord(
                fieldAt(fields, 0),
                fieldAt(fields, 1),
                fieldAt(fields, 2),
                fieldAt(fields, 3),
                fieldAt(fields, 4),
                fieldAt(fields, 5),
                fieldAt(fields, 6),
                fieldAt(fields, 7),
                fieldAt(fields, 8),
                fieldAt(fields, 9),
                fieldAt(fields, 10),
                fieldAt(fields, 11),
                fieldAt(fields, 12),
                fieldAt(fields, MAX_FIELD_COUNT - 1));
    }

    private String fieldAt(String[] fields, int index) {
        if (index >= fields.length) {
            return "";
        }
        String value = fields[index];
        return value == null ? "" : value.trim();
    }
}
