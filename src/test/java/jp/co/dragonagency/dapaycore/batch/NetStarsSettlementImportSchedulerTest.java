package jp.co.dragonagency.dapaycore.batch;

import jp.co.dragonagency.dapaycore.dto.NetStarsImportResult;
import jp.co.dragonagency.dapaycore.service.NetStarsSettlementImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link NetStarsSettlementImportScheduler} の単体テスト。
 * 単体テスト仕様書_還元データ取込履歴照会_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T41〜T42（■ テストケース一覧）に対応する。
 * INPUT データ: C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_還元データ取込履歴照会_v1.00.xlsx
 */
@ExtendWith(MockitoExtension.class)
class NetStarsSettlementImportSchedulerTest {

    @Mock
    private NetStarsSettlementImportService importService;

    @InjectMocks
    private NetStarsSettlementImportScheduler scheduler;

    @Test
    void T41_importDaily_runScheduledImportを呼ぶ() {
        when(importService.runScheduledImport()).thenReturn(new NetStarsImportResult(
                1L, "SUCCESS", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1),
                0, 0, 0, 1, "取込完了"));

        scheduler.importDaily();

        verify(importService).runScheduledImport();
    }

    @Test
    void T42_importDaily_取込で例外が発生しても例外を投げない() {
        when(importService.runScheduledImport())
                .thenThrow(new RuntimeException("想定外エラー"));

        assertDoesNotThrow(() -> scheduler.importDaily());
    }
}
