package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.NetStarsImportControlView;
import jp.co.dragonagency.dapaycore.dto.NetStarsImportHistoryListItemDto;
import jp.co.dragonagency.dapaycore.dto.NetStarsImportResult;
import jp.co.dragonagency.dapaycore.model.NetStarsImportConfig;
import jp.co.dragonagency.dapaycore.model.NetStarsImportHistory;
import jp.co.dragonagency.dapaycore.repository.NetStarsImportHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link NetStarsSettlementImportService} の単体テスト。
 * 単体テスト仕様書_還元データ取込履歴照会_自社mPOSキャッシュレス管理システム_v1.00.xlsx の 項番 T26〜T40（■ テストケース一覧）に対応する。
 * 日付境界の検証のため、時刻は 2026-09-02 14:00（Asia/Tokyo）に固定する。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NetStarsSettlementImportServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-02T05:00:00Z"), ZONE);
    private static final LocalDate YESTERDAY = LocalDate.of(2026, 9, 1);
    private static final int AUTO_RESUME_DAYS = 5;

    @Mock
    private NetStarsReductionDataApiClient apiClient;
    @Mock
    private NetStarsReductionDataCsvParser csvParser;
    @Mock
    private NetStarsSettlementDetailWriter detailWriter;
    @Mock
    private NetStarsImportHistoryRepository historyRepository;
    @Mock
    private NetStarsImportConfigService configService;

    private NetStarsSettlementImportService service;

    @BeforeEach
    void setUp() {
        service = new NetStarsSettlementImportService(
                apiClient, csvParser, detailWriter, historyRepository,
                configService, FIXED_CLOCK, AUTO_RESUME_DAYS);
        when(historyRepository.save(any(NetStarsImportHistory.class)))
                .thenAnswer(inv -> {
                    NetStarsImportHistory history = inv.getArgument(0);
                    if (history.getId() == 0L) {
                        history.setId(1L);
                    }
                    return history;
                });
        when(configService.getOrCreate()).thenReturn(enabledConfig());
    }

    // ============ getControlView ============

    @Test
    void T26_getControlView_稼働中のとき停止日時は空でenabledがtrue() {
        when(apiClient.isConfigured()).thenReturn(true);

        NetStarsImportControlView view = service.getControlView();

        assertTrue(view.enabled());
        assertTrue(view.apiConfigured());
        assertEquals("", view.stoppedAtText());
        assertEquals("", view.autoResumeText());
    }

    @Test
    void T27_getControlView_停止中のとき停止日時と自動再開予定日を返す() {
        NetStarsImportConfig stopped = enabledConfig();
        stopped.setEnabled(false);
        stopped.setStoppedAt(LocalDateTime.of(2026, 8, 30, 9, 0));
        when(configService.getOrCreate()).thenReturn(stopped);

        NetStarsImportControlView view = service.getControlView();

        assertFalse(view.enabled());
        assertEquals("2026/08/30 09:00", view.stoppedAtText());
        assertEquals("2026/09/04", view.autoResumeText());
    }

    // ============ runScheduledImport（稼働中） ============

    @Test
    void T28_runScheduledImport_前回成功日の翌日から前日までを1窓で取得する()
            throws IOException, InterruptedException {
        stubConfiguredEmptyImport();
        when(historyRepository.findTopByStatusOrderByEndDateDesc(
                NetStarsImportHistory.STATUS_SUCCESS))
                .thenReturn(Optional.of(historyWithEndDate(LocalDate.of(2026, 8, 31))));

        NetStarsImportResult result = service.runScheduledImport();

        assertEquals(NetStarsImportHistory.STATUS_SUCCESS, result.status());
        verify(apiClient).fetchCsvPage(eq(YESTERDAY), eq(YESTERDAY), eq(1));
    }

    @Test
    void T29_runScheduledImport_成功履歴が無いとき既定3日分を1窓で取得する()
            throws IOException, InterruptedException {
        stubConfiguredEmptyImport();
        when(historyRepository.findTopByStatusOrderByEndDateDesc(
                NetStarsImportHistory.STATUS_SUCCESS)).thenReturn(Optional.empty());

        service.runScheduledImport();

        verify(apiClient).fetchCsvPage(
                eq(LocalDate.of(2026, 8, 30)), eq(YESTERDAY), eq(1));
    }

    @Test
    void T30_runScheduledImport_5日を超える取りこぼしは5日窓に分割して取得する()
            throws IOException, InterruptedException {
        stubConfiguredEmptyImport();
        when(historyRepository.findTopByStatusOrderByEndDateDesc(
                NetStarsImportHistory.STATUS_SUCCESS))
                .thenReturn(Optional.of(historyWithEndDate(LocalDate.of(2026, 8, 20))));

        NetStarsImportResult result = service.runScheduledImport();

        // naturalBegin=08-21 は 10 日前（08-23）より古いので 08-23 に丸め、
        // 08-23〜09-01 を [08-23,08-27] と [08-28,09-01] の 2 窓で取得する
        verify(apiClient).fetchCsvPage(
                eq(LocalDate.of(2026, 8, 23)), eq(LocalDate.of(2026, 8, 27)), eq(1));
        verify(apiClient).fetchCsvPage(
                eq(LocalDate.of(2026, 8, 28)), eq(LocalDate.of(2026, 9, 1)), eq(1));
        // 取得できなかった 08-21〜08-22 は結果メッセージで警告される
        assertTrue(result.message().contains("2026/08/21"));
        assertTrue(result.message().contains("システム管理者"));
    }

    @Test
    void T31_runScheduledImport_API未設定のときSKIPPED履歴を残す() {
        when(apiClient.isConfigured()).thenReturn(false);
        when(historyRepository.findTopByStatusOrderByEndDateDesc(
                NetStarsImportHistory.STATUS_SUCCESS)).thenReturn(Optional.empty());

        NetStarsImportResult result = service.runScheduledImport();

        assertEquals(NetStarsImportHistory.STATUS_SKIPPED, result.status());
        verify(apiClient, never()).getPageSize();
    }

    @Test
    void T32_runScheduledImport_取得対象の新しい日付が無いときスキップする() {
        when(apiClient.isConfigured()).thenReturn(true);
        when(historyRepository.findTopByStatusOrderByEndDateDesc(
                NetStarsImportHistory.STATUS_SUCCESS))
                .thenReturn(Optional.of(historyWithEndDate(LocalDate.of(2026, 9, 5))));

        NetStarsImportResult result = service.runScheduledImport();

        assertEquals(NetStarsImportHistory.STATUS_SKIPPED, result.status());
    }

    @Test
    void T33_runScheduledImport_取込中に例外が発生したときFAILED履歴を残す()
            throws IOException, InterruptedException {
        when(apiClient.isConfigured()).thenReturn(true);
        when(apiClient.getPageSize()).thenReturn(1000);
        when(historyRepository.findTopByStatusOrderByEndDateDesc(
                NetStarsImportHistory.STATUS_SUCCESS)).thenReturn(Optional.empty());
        when(apiClient.fetchCsvPage(any(), any(), anyInt()))
                .thenThrow(new IOException("HTTP 500"));

        NetStarsImportResult result = service.runScheduledImport();

        assertEquals(NetStarsImportHistory.STATUS_FAILED, result.status());
    }

    @Test
    void T34_runScheduledImport_取込種別はSCHEDULEDで記録される()
            throws IOException, InterruptedException {
        stubConfiguredEmptyImport();
        when(historyRepository.findTopByStatusOrderByEndDateDesc(
                NetStarsImportHistory.STATUS_SUCCESS)).thenReturn(Optional.empty());

        service.runScheduledImport();

        assertEquals(NetStarsImportHistory.IMPORT_TYPE_SCHEDULED,
                lastSavedHistory().getImportType());
    }

    // ============ runScheduledImport（停止中） ============

    @Test
    void T35_runScheduledImport_停止中で規定日数未経過のとき停止中メッセージでスキップする()
            throws IOException, InterruptedException {
        NetStarsImportConfig stopped = enabledConfig();
        stopped.setEnabled(false);
        stopped.setStoppedAt(LocalDateTime.of(2026, 9, 1, 9, 0));
        when(configService.getOrCreate()).thenReturn(stopped);

        NetStarsImportResult result = service.runScheduledImport();

        assertEquals(NetStarsImportHistory.STATUS_SKIPPED, result.status());
        assertTrue(result.message().contains("受信停止中"));
        verify(configService, never()).markAutoResumed();
        verify(apiClient, never()).fetchCsvPage(any(), any(), anyInt());
    }

    @Test
    void T36_runScheduledImport_停止から規定日数が経過したとき自動再開して取込する()
            throws IOException, InterruptedException {
        NetStarsImportConfig stopped = enabledConfig();
        stopped.setEnabled(false);
        stopped.setStoppedAt(LocalDateTime.of(2026, 8, 25, 9, 0));
        when(configService.getOrCreate()).thenReturn(stopped);
        stubConfiguredEmptyImport();
        when(historyRepository.findTopByStatusOrderByEndDateDesc(
                NetStarsImportHistory.STATUS_SUCCESS)).thenReturn(Optional.empty());

        NetStarsImportResult result = service.runScheduledImport();

        verify(configService).markAutoResumed();
        assertEquals(NetStarsImportHistory.STATUS_SUCCESS, result.status());
        assertEquals(NetStarsImportHistory.IMPORT_TYPE_AUTO_RESUME,
                lastSavedHistory().getImportType());
    }

    // ============ STOP / RESTART ============

    @Test
    void T37_stopImport_configServiceへ委譲する() {
        service.stopImport("user001");

        verify(configService).stop("user001");
    }

    @Test
    void T38_restartImport_configServiceの再開へ委譲する() {
        service.restartImport("user001");

        verify(configService).restart("user001");
    }

    @Test
    void T39_triggerRestartImport_RESTART種別で取込する()
            throws IOException, InterruptedException {
        stubConfiguredEmptyImport();
        when(historyRepository.findTopByStatusOrderByEndDateDesc(
                NetStarsImportHistory.STATUS_SUCCESS))
                .thenReturn(Optional.of(historyWithEndDate(LocalDate.of(2026, 8, 31))));

        service.triggerRestartImport();

        assertEquals(NetStarsImportHistory.IMPORT_TYPE_RESTART,
                lastSavedHistory().getImportType());
    }

    // ============ findHistoryForList ============

    @Test
    void T40_findHistoryForList_履歴をDTOへ整形して返す() {
        NetStarsImportHistory history = new NetStarsImportHistory();
        history.setId(9L);
        history.setImportType(NetStarsImportHistory.IMPORT_TYPE_SCHEDULED);
        history.setStatus(NetStarsImportHistory.STATUS_SUCCESS);
        history.setBeginDate(LocalDate.of(2026, 8, 30));
        history.setEndDate(LocalDate.of(2026, 9, 1));
        history.setFetchedCount(12);
        history.setStartedAt(LocalDateTime.of(2026, 9, 2, 10, 0, 0));
        when(historyRepository.findAllByOrderByStartedAtDesc(any()))
                .thenReturn(List.of(history));

        List<NetStarsImportHistoryListItemDto> list = service.findHistoryForList();

        assertEquals(1, list.size());
        NetStarsImportHistoryListItemDto dto = list.get(0);
        assertEquals("自動", dto.getImportTypeLabel());
        assertEquals("成功", dto.getStatusLabel());
        assertEquals("2026/08/30 〜 2026/09/01", dto.getTargetPeriod());
        assertEquals("2026/09/02 10:00:00", dto.getStartedAt());
    }

    // ============ ヘルパー ============

    private void stubConfiguredEmptyImport() throws IOException, InterruptedException {
        when(apiClient.isConfigured()).thenReturn(true);
        when(apiClient.getPageSize()).thenReturn(1000);
        when(apiClient.fetchCsvPage(any(), any(), anyInt())).thenReturn("");
        when(csvParser.parse(any())).thenReturn(List.of());
    }

    private NetStarsImportHistory lastSavedHistory() {
        ArgumentCaptor<NetStarsImportHistory> captor =
                ArgumentCaptor.forClass(NetStarsImportHistory.class);
        verify(historyRepository, Mockito.atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    private static NetStarsImportConfig enabledConfig() {
        NetStarsImportConfig config = new NetStarsImportConfig();
        config.setConfigKey(NetStarsImportConfig.KEY_DAILY_IMPORT);
        config.setEnabled(true);
        return config;
    }

    private static NetStarsImportHistory historyWithEndDate(LocalDate endDate) {
        NetStarsImportHistory history = new NetStarsImportHistory();
        history.setStatus(NetStarsImportHistory.STATUS_SUCCESS);
        history.setEndDate(endDate);
        return history;
    }
}
