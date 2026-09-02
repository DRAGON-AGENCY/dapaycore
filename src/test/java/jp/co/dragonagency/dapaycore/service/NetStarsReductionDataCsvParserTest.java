package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.NetStarsCsvRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link NetStarsReductionDataCsvParser} の単体テスト。
 * 単体テスト仕様書_還元データ取込履歴照会_自社mPOSキャッシュレス管理システム_v1.00.xlsx の 項番 T1〜T10（■ テストケース一覧）に対応する。
 */
class NetStarsReductionDataCsvParserTest {

    private static final String HEADER_13 =
            "ShopCode,ShopName,TradeTime,MchTradeNo,DevTradeNo,Type,PayType,"
            + "Amount,Currency,DeviceId,DeviceNo,OutTradeNo,Detail";
    private static final String HEADER_14 = HEADER_13 + ", Attach";

    private final NetStarsReductionDataCsvParser parser =
            new NetStarsReductionDataCsvParser();

    @Test
    void T1_parse_ヘッダ行はレコードに含めない() {
        List<NetStarsCsvRecord> records = parser.parse(HEADER_13 + "\r\n");

        assertTrue(records.isEmpty());
    }

    @Test
    void T2_parse_13列のデータ行を1件のレコードとして解析する() {
        String csv = HEADER_13 + "\r\n"
                + "90000000200000064,オンラインテスト店舗,20221101100953,"
                + "NETS001XP1221101100931354,1221101100931354,PAY,PAYPAYMPM,"
                + "240,JPY,WEB,WEB,NETS001XP1221101100931354,\r\n";

        List<NetStarsCsvRecord> records = parser.parse(csv);

        assertEquals(1, records.size());
        NetStarsCsvRecord record = records.get(0);
        assertEquals("90000000200000064", record.shopCode());
        assertEquals("オンラインテスト店舗", record.shopName());
        assertEquals("20221101100953", record.tradeTime());
        assertEquals("PAY", record.type());
        assertEquals("PAYPAYMPM", record.payType());
        assertEquals("240", record.amount());
        assertEquals("JPY", record.currency());
        assertEquals("NETS001XP1221101100931354", record.outTradeNo());
        assertEquals("", record.detail());
        assertEquals("", record.attach());
    }

    @Test
    void T3_parse_14列目のAttachを読み取る() {
        String csv = HEADER_14 + "\r\n"
                + "00001813,TestShop,20170206151558,TESTMIKI1563P20170206151553,"
                + "P20170206151553,PAY,MICROPAY,108,JPY,STARPAY12345,1563,"
                + "TESTMIKI1563P20170206151553,商品カテゴリ,付加情報XYZ\r\n";

        List<NetStarsCsvRecord> records = parser.parse(csv);

        assertEquals("商品カテゴリ", records.get(0).detail());
        assertEquals("付加情報XYZ", records.get(0).attach());
    }

    @Test
    void T4_parse_REFUND行を解析する() {
        String csv = HEADER_13 + "\n"
                + "90000000200000123,tfps,20221101134016,NETS1VNA0004R20221101134016,"
                + "R20221101134016,REFUND,PAYPAY,1,JPY,DEVICE,0004,"
                + "NETS1VNA0004P20221101133947,\n";

        List<NetStarsCsvRecord> records = parser.parse(csv);

        assertEquals("REFUND", records.get(0).type());
        assertEquals("NETS1VNA0004R20221101134016", records.get(0).mchTradeNo());
        assertEquals("NETS1VNA0004P20221101133947", records.get(0).outTradeNo());
    }

    @Test
    void T5_parse_BOM付き先頭でもヘッダを認識する() {
        String csv = "﻿" + HEADER_13 + "\r\n"
                + "S,N,20221101100953,M,D,PAY,PAYPAY,10,JPY,DID,0001,OUT,\r\n";

        List<NetStarsCsvRecord> records = parser.parse(csv);

        assertEquals(1, records.size());
        assertEquals("S", records.get(0).shopCode());
    }

    @Test
    void T6_parse_空行は読み飛ばす() {
        String csv = HEADER_13 + "\r\n"
                + "S,N,20221101100953,M,D,PAY,PAYPAY,10,JPY,DID,0001,OUT,\r\n"
                + "\r\n";

        List<NetStarsCsvRecord> records = parser.parse(csv);

        assertEquals(1, records.size());
    }

    @Test
    void T7_parse_項目数が不足した行は読み飛ばす() {
        String csv = HEADER_13 + "\r\n"
                + "S,N,20221101100953,M,D,PAY\r\n"
                + "S2,N2,20221101100954,M2,D2,PAY,PAYPAY,20,JPY,DID,0001,OUT2,\r\n";

        List<NetStarsCsvRecord> records = parser.parse(csv);

        assertEquals(1, records.size());
        assertEquals("S2", records.get(0).shopCode());
    }

    @Test
    void T8_parse_取引データが無くヘッダのみのとき空リストを返す() {
        List<NetStarsCsvRecord> records = parser.parse(HEADER_14 + "\r\n");

        assertTrue(records.isEmpty());
    }

    @Test
    void T9_parse_nullや空文字のとき空リストを返す() {
        assertTrue(parser.parse(null).isEmpty());
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse("   ").isEmpty());
    }

    @Test
    void T10_parse_各項目の前後空白を除去する() {
        String csv = HEADER_13 + "\r\n"
                + " S , N ,20221101100953, M , D , PAY , PAYPAY , 10 , JPY ,"
                + " DID , 0001 , OUT , \r\n";

        NetStarsCsvRecord record = parser.parse(csv).get(0);

        assertEquals("S", record.shopCode());
        assertEquals("PAY", record.type());
        assertEquals("10", record.amount());
    }
}
