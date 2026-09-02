package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.NetStarsCsvRecord;
import jp.co.dragonagency.dapaycore.model.NetStarsSettlementDetail;
import jp.co.dragonagency.dapaycore.repository.NetStarsSettlementDetailRepository;
import jp.co.dragonagency.dapaycore.service.NetStarsSettlementDetailWriter.PageWriteResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link NetStarsSettlementDetailWriter} の単体テスト。
 * 単体テスト仕様書_還元データ取込履歴照会_自社mPOSキャッシュレス管理システム_v1.00.xlsx の 項番 T14〜T21（■ テストケース一覧）に対応する。
 */
@ExtendWith(MockitoExtension.class)
class NetStarsSettlementDetailWriterTest {

    @Mock
    private NetStarsSettlementDetailRepository detailRepository;

    @InjectMocks
    private NetStarsSettlementDetailWriter writer;

    @Test
    void T14_buildDedupKey_サーバー取引番号があればそれを使用する() {
        NetStarsCsvRecord record = record("MCH123", "OUT999", "PAY", "20221101100953");

        assertEquals("MCH123", writer.buildDedupKey(record));
    }

    @Test
    void T15_buildDedupKey_サーバー取引番号が空ならMch取引番号と種類と時間を連結する() {
        NetStarsCsvRecord record = record("", "OUT999", "REVOKED", "20221101100953");

        assertEquals("OUT999|REVOKED|20221101100953", writer.buildDedupKey(record));
    }

    @Test
    void T16_writePage_未登録の取引は新規登録され新規件数が1になる() {
        when(detailRepository.findByDedupKey("MCH1")).thenReturn(Optional.empty());
        when(detailRepository.save(any(NetStarsSettlementDetail.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PageWriteResult result = writer.writePage(
                List.of(record("MCH1", "OUT1", "PAY", "20221101100953")), 7L);

        assertEquals(1, result.inserted());
        assertEquals(0, result.updated());
    }

    @Test
    void T17_writePage_登録済みの取引は上書きされ更新件数が1になる() {
        NetStarsSettlementDetail existing = new NetStarsSettlementDetail();
        existing.setDedupKey("MCH1");
        existing.setFirstImportedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(detailRepository.findByDedupKey("MCH1"))
                .thenReturn(Optional.of(existing));
        when(detailRepository.save(any(NetStarsSettlementDetail.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PageWriteResult result = writer.writePage(
                List.of(record("MCH1", "OUT1", "REFUND", "20221101100953")), 7L);

        assertEquals(0, result.inserted());
        assertEquals(1, result.updated());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0),
                existing.getFirstImportedAt());
        assertEquals("REFUND", existing.getTradeType());
        assertEquals(7L, existing.getImportHistoryId());
    }

    @Test
    void T18_writePage_CSV項目がエンティティへ正しく写される() {
        when(detailRepository.findByDedupKey(any())).thenReturn(Optional.empty());
        when(detailRepository.save(any(NetStarsSettlementDetail.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        NetStarsCsvRecord record = new NetStarsCsvRecord(
                "SHOP1", "テスト店舗", "20221101100953", "MCH1", "DEV1",
                "PAY", "PAYPAYMPM", "240", "JPY", "DEVICEID", "0004",
                "OUT1", "商品詳細", "付加情報");

        writer.writePage(List.of(record), 3L);

        ArgumentCaptor<NetStarsSettlementDetail> captor =
                ArgumentCaptor.forClass(NetStarsSettlementDetail.class);
        verify(detailRepository).save(captor.capture());
        NetStarsSettlementDetail saved = captor.getValue();
        assertEquals("SHOP1", saved.getShopCode());
        assertEquals("テスト店舗", saved.getShopName());
        assertEquals("PAYPAYMPM", saved.getPayType());
        assertEquals(240L, saved.getAmount());
        assertEquals("OUT1", saved.getOutTradeNo());
        assertEquals("商品詳細", saved.getDetail());
        assertEquals("付加情報", saved.getAttach());
        assertEquals(LocalDateTime.of(2022, 11, 1, 10, 9, 53), saved.getTradeTime());
    }

    @Test
    void T19_writePage_取引時間が14桁でないときtradeTimeはnullで生値のみ保持する() {
        when(detailRepository.findByDedupKey(any())).thenReturn(Optional.empty());
        when(detailRepository.save(any(NetStarsSettlementDetail.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        writer.writePage(
                List.of(record("MCH1", "OUT1", "PAY", "2020221101111454")), 1L);

        ArgumentCaptor<NetStarsSettlementDetail> captor =
                ArgumentCaptor.forClass(NetStarsSettlementDetail.class);
        verify(detailRepository).save(captor.capture());
        assertNull(captor.getValue().getTradeTime());
        assertEquals("2020221101111454", captor.getValue().getTradeTimeRaw());
    }

    @Test
    void T20_writePage_取引金額が数値でないとき0として保存する() {
        when(detailRepository.findByDedupKey(any())).thenReturn(Optional.empty());
        when(detailRepository.save(any(NetStarsSettlementDetail.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        NetStarsCsvRecord record = new NetStarsCsvRecord(
                "SHOP1", "N", "20221101100953", "MCH1", "D",
                "PAY", "PAYPAY", "true", "JPY", "DID", "0004", "OUT1", "", "");

        writer.writePage(List.of(record), 1L);

        ArgumentCaptor<NetStarsSettlementDetail> captor =
                ArgumentCaptor.forClass(NetStarsSettlementDetail.class);
        verify(detailRepository).save(captor.capture());
        assertEquals(0L, captor.getValue().getAmount());
    }

    @Test
    void T21_writePage_空リストのとき保存を呼ばず件数は0になる() {
        PageWriteResult result = writer.writePage(List.of(), 1L);

        assertEquals(0, result.inserted());
        assertEquals(0, result.updated());
        verify(detailRepository, never()).save(any());
    }

    private static NetStarsCsvRecord record(
            String mchTradeNo, String outTradeNo, String type, String tradeTime) {
        return new NetStarsCsvRecord(
                "SHOP1", "店舗名", tradeTime, mchTradeNo, "DEV1",
                type, "PAYPAY", "100", "JPY", "DEVICEID", "0004",
                outTradeNo, "", "");
    }
}
