package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.NetStarsCsvRecord;
import jp.co.dragonagency.dapaycore.dto.NetStarsImportControlView;
import jp.co.dragonagency.dapaycore.dto.NetStarsImportHistoryListItemDto;
import jp.co.dragonagency.dapaycore.dto.NetStarsImportResult;
import jp.co.dragonagency.dapaycore.model.NetStarsImportConfig;
import jp.co.dragonagency.dapaycore.model.NetStarsImportHistory;
import jp.co.dragonagency.dapaycore.repository.NetStarsImportHistoryRepository;
import jp.co.dragonagency.dapaycore.service.NetStarsSettlementDetailWriter.PageWriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ネットスターズ還元データ（取引明細）の取込処理を統括するサービス。
 * 受信はアプリケーション（日次スケジューラ）が一元管理し、任意期間を指定する
 * 手動取込は提供しない。取りこぼしは常に「前回成功日の翌日〜前日」という
 * 前方向の連続した期間を取得することで補完する。
 *
 * <p>トラブル時は画面の STOP / RESTART で受信を停止・再開できる。停止から
 * 規定日数（{@code netstars.import.auto-resume-days}・既定 5 日）が経過すると
 * スケジューラが自動で再開する。停止していた期間は再開時にまとめて取得する。</p>
 *
 * <p>StarPay 還元データ項目仕様書 v1.0.11「３－２－３. 検索制限」に従い、
 * 取得できるのは前日 23:59:59 までのデータで、begindate は当日から 10 日前まで、
 * 1 回の検索期間は最大 5 日とする。5 日を超える取りこぼしは 5 日ずつの窓に
 * 分割してループ取得し、10 日前までを補完する。</p>
 */
@Service
public class NetStarsSettlementImportService {

    private static final Logger log =
            LoggerFactory.getLogger(NetStarsSettlementImportService.class);

    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_MINUTE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** begindate に設定できる最も古い日付（当日からの日数）。 */
    private static final int MAX_LOOKBACK_DAYS = 10;

    /** 1 回の API 検索で指定できる期間の最大日数。 */
    private static final int MAX_WINDOW_DAYS = 5;

    /** 過去の成功履歴が無い場合に初回実行が遡って取得する日数。 */
    private static final int DEFAULT_INITIAL_LOOKBACK_DAYS = 3;

    /** 1 つの検索期間でたどるページ数の上限（無限ループ防止）。 */
    private static final int MAX_PAGES = 200;

    /** 1 回の実行でたどる検索期間（窓）の数の上限（無限ループ防止）。 */
    private static final int MAX_WINDOWS = 5;

    /** 取込履歴一覧に表示する最大件数。 */
    private static final int HISTORY_LIST_SIZE = 50;

    private final NetStarsReductionDataApiClient apiClient;
    private final NetStarsReductionDataCsvParser csvParser;
    private final NetStarsSettlementDetailWriter detailWriter;
    private final NetStarsImportHistoryRepository historyRepository;
    private final NetStarsImportConfigService configService;
    private final Clock clock;
    private final int autoResumeAfterDays;

    public NetStarsSettlementImportService(
            NetStarsReductionDataApiClient apiClient,
            NetStarsReductionDataCsvParser csvParser,
            NetStarsSettlementDetailWriter detailWriter,
            NetStarsImportHistoryRepository historyRepository,
            NetStarsImportConfigService configService,
            Clock clock,
            @Value("${netstars.import.auto-resume-days:5}") int autoResumeAfterDays) {
        this.apiClient = apiClient;
        this.csvParser = csvParser;
        this.detailWriter = detailWriter;
        this.historyRepository = historyRepository;
        this.configService = configService;
        this.clock = clock;
        this.autoResumeAfterDays = Math.max(1, autoResumeAfterDays);
    }

    /**
     * API 接続情報が設定済みかどうかを返す。
     *
     * @return 設定済みの場合は true
     */
    public boolean isApiConfigured() {
        return apiClient.isConfigured();
    }

    /**
     * 画面表示用の受信稼働状態を返す。
     *
     * @return 受信稼働状態
     */
    public NetStarsImportControlView getControlView() {
        NetStarsImportConfig config = configService.getOrCreate();
        LocalDateTime stoppedAt = config.getStoppedAt();
        String stoppedAtText = stoppedAt == null
                ? "" : DISPLAY_DATE_MINUTE_FORMAT.format(stoppedAt);
        String autoResumeText = stoppedAt == null
                ? "" : DISPLAY_DATE_FORMAT.format(
                        stoppedAt.plusDays(autoResumeAfterDays).toLocalDate());
        return new NetStarsImportControlView(
                config.isEnabled(),
                apiClient.isConfigured(),
                config.isAutoResumed(),
                stoppedAtText,
                autoResumeText);
    }

    /**
     * 取込履歴を新しい順に取得し、画面表示用の一覧へ整形して返す。
     *
     * @return 取込履歴一覧
     */
    @Transactional(readOnly = true)
    public List<NetStarsImportHistoryListItemDto> findHistoryForList() {
        List<NetStarsImportHistory> histories =
                historyRepository.findAllByOrderByStartedAtDesc(
                        PageRequest.of(0, HISTORY_LIST_SIZE));
        List<NetStarsImportHistoryListItemDto> list = new ArrayList<>();
        for (NetStarsImportHistory history : histories) {
            list.add(toListItem(history));
        }
        return list;
    }

    /**
     * 受信を停止する（画面の STOP ボタン）。
     *
     * @param userId 操作した運用担当者の社員番号
     */
    public void stopImport(String userId) {
        configService.stop(userId);
    }

    /**
     * 受信を再開する（画面の RESTART ボタン）。フラグの更新のみを行う。
     * 停止していた期間の取込は呼び出し元が {@link #triggerRestartImport()} を
     * 別途呼び出して非同期で開始する（{@code @Async} を効かせるため自己呼び出しにしない）。
     *
     * @param userId 操作した運用担当者の社員番号
     */
    public void restartImport(String userId) {
        configService.restart(userId);
    }

    /**
     * 日次スケジューラから呼び出す取込を実行する。
     * 受信停止中は取込を行わず、停止から規定日数が経過している場合は自動再開する。
     *
     * @return 取込結果
     */
    public NetStarsImportResult runScheduledImport() {
        NetStarsImportConfig config = configService.getOrCreate();
        if (!config.isEnabled()) {
            if (isAutoResumeDue(config)) {
                log.warn("還元データ受信: 停止から {} 日が経過したため自動再開します。",
                        autoResumeAfterDays);
                configService.markAutoResumed();
                return runCatchUp(NetStarsImportHistory.IMPORT_TYPE_AUTO_RESUME);
            }
            return recordStopped(config);
        }
        return runCatchUp(NetStarsImportHistory.IMPORT_TYPE_SCHEDULED);
    }

    /**
     * RESTART 操作に伴う取込を非同期で実行する。
     * スケジューラのスレッドや HTTP 応答をブロックしないよう別スレッドで動かす。
     */
    @Async
    public void triggerRestartImport() {
        try {
            runCatchUp(NetStarsImportHistory.IMPORT_TYPE_RESTART);
        } catch (RuntimeException e) {
            log.error("還元データ RESTART 後の取込で想定外のエラーが発生しました: {}",
                    e.getMessage(), e);
        }
    }

    private boolean isAutoResumeDue(NetStarsImportConfig config) {
        LocalDateTime stoppedAt = config.getStoppedAt();
        if (stoppedAt == null) {
            return false;
        }
        LocalDateTime resumeAt = stoppedAt.plusDays(autoResumeAfterDays);
        return !LocalDateTime.now(clock).isBefore(resumeAt);
    }

    private NetStarsImportResult recordStopped(NetStarsImportConfig config) {
        LocalDate endDate = LocalDate.now(clock).minusDays(1);
        NetStarsImportHistory history = createRunningHistory(
                NetStarsImportHistory.IMPORT_TYPE_SCHEDULED, endDate, endDate);
        String message = "受信停止中のため取込をスキップしました。";
        LocalDateTime stoppedAt = config.getStoppedAt();
        if (stoppedAt != null) {
            message += "（" + DISPLAY_DATE_MINUTE_FORMAT.format(stoppedAt) + " に停止 / "
                    + DISPLAY_DATE_FORMAT.format(
                            stoppedAt.plusDays(autoResumeAfterDays).toLocalDate())
                    + " 頃に自動再開）";
        }
        log.info("還元データ受信: {}", message);
        return finishSkipped(history, message);
    }

    private NetStarsImportResult runCatchUp(String importType) {
        LocalDate today = LocalDate.now(clock);
        LocalDate endDate = today.minusDays(1);
        LocalDate oldestAllowed = today.minusDays(MAX_LOOKBACK_DAYS);
        LocalDate naturalBegin = resolveNaturalBeginDate(endDate);
        LocalDate beginDate = naturalBegin.isBefore(oldestAllowed)
                ? oldestAllowed : naturalBegin;

        NetStarsImportHistory history = createRunningHistory(importType,
                beginDate.isAfter(endDate) ? endDate : beginDate, endDate);

        if (beginDate.isAfter(endDate)) {
            return finishSkipped(history,
                    "取得対象の新しい日付がないため取込をスキップしました。");
        }
        if (!apiClient.isConfigured()) {
            log.warn("還元データ取込: API 接続情報が未設定のため取込をスキップします。");
            return finishSkipped(history,
                    "ネットスターズ還元データ API の接続情報（ドメイン・企業コード・key）"
                    + "が未設定のため取込をスキップしました。");
        }

        String lostWarning = buildLostRangeWarning(naturalBegin, oldestAllowed);
        try {
            ImportCounts counts =
                    fetchAndPersistWindowed(history.getId(), beginDate, endDate);
            return finishSuccess(history, counts, lostWarning);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return finishFailed(history, "取込処理が中断されました。");
        } catch (Exception e) {
            log.error("還元データ取込に失敗しました。 beginDate={} endDate={}: {}",
                    beginDate, endDate, e.getMessage(), e);
            return finishFailed(history, e.getMessage());
        }
    }

    private LocalDate resolveNaturalBeginDate(LocalDate endDate) {
        Optional<NetStarsImportHistory> lastSuccess = historyRepository
                .findTopByStatusOrderByEndDateDesc(
                        NetStarsImportHistory.STATUS_SUCCESS);
        return lastSuccess
                .map(history -> history.getEndDate().plusDays(1))
                .orElse(endDate.minusDays(DEFAULT_INITIAL_LOOKBACK_DAYS - 1L));
    }

    private String buildLostRangeWarning(
            LocalDate naturalBegin, LocalDate oldestAllowed) {
        if (!naturalBegin.isBefore(oldestAllowed)) {
            return "";
        }
        String lostFrom = DISPLAY_DATE_FORMAT.format(naturalBegin);
        String lostTo = DISPLAY_DATE_FORMAT.format(oldestAllowed.minusDays(1));
        log.warn("還元データ取込: {} 〜 {} は API の遡及制限（{} 日）により取得できません。"
                + " システム管理者へ連絡してください。",
                lostFrom, lostTo, MAX_LOOKBACK_DAYS);
        return lostFrom + " 〜 " + lostTo
                + " は API 制限により取得できませんでした（システム管理者へ連絡してください）。";
    }

    private ImportCounts fetchAndPersistWindowed(
            long historyId, LocalDate beginDate, LocalDate endDate)
            throws IOException, InterruptedException {
        int fetched = 0;
        int inserted = 0;
        int updated = 0;
        int pageCount = 0;

        LocalDate windowStart = beginDate;
        int windows = 0;
        while (!windowStart.isAfter(endDate) && windows < MAX_WINDOWS) {
            LocalDate windowEnd = windowStart.plusDays(MAX_WINDOW_DAYS - 1L);
            if (windowEnd.isAfter(endDate)) {
                windowEnd = endDate;
            }
            ImportCounts window =
                    fetchWindow(historyId, windowStart, windowEnd);
            fetched += window.fetched();
            inserted += window.inserted();
            updated += window.updated();
            pageCount += window.pageCount();
            windowStart = windowEnd.plusDays(1);
            windows++;
        }
        if (!windowStart.isAfter(endDate)) {
            log.warn("還元データ取込: 検索期間の窓数が上限（{}）に達したため打ち切りました。",
                    MAX_WINDOWS);
        }
        return new ImportCounts(fetched, inserted, updated, pageCount);
    }

    private ImportCounts fetchWindow(
            long historyId, LocalDate beginDate, LocalDate endDate)
            throws IOException, InterruptedException {
        int pageSize = apiClient.getPageSize();
        int pageIndex = 1;
        int fetched = 0;
        int inserted = 0;
        int updated = 0;
        int pageCount = 0;

        while (pageIndex <= MAX_PAGES) {
            String csvBody = apiClient.fetchCsvPage(beginDate, endDate, pageIndex);
            List<NetStarsCsvRecord> records = csvParser.parse(csvBody);
            pageCount++;
            fetched += records.size();

            if (!records.isEmpty()) {
                PageWriteResult result =
                        detailWriter.writePage(records, historyId);
                inserted += result.inserted();
                updated += result.updated();
            }
            if (records.size() < pageSize) {
                break;
            }
            pageIndex++;
        }
        if (pageIndex > MAX_PAGES) {
            log.warn("還元データ取込: ページ数が上限（{}）に達したため打ち切りました。",
                    MAX_PAGES);
        }
        return new ImportCounts(fetched, inserted, updated, pageCount);
    }

    private NetStarsImportHistory createRunningHistory(
            String importType, LocalDate beginDate, LocalDate endDate) {
        NetStarsImportHistory history = new NetStarsImportHistory();
        history.setImportType(importType);
        history.setStatus(NetStarsImportHistory.STATUS_RUNNING);
        history.setBeginDate(beginDate);
        history.setEndDate(endDate);
        history.setStartedAt(LocalDateTime.now(clock));
        return historyRepository.save(history);
    }

    private NetStarsImportResult finishSuccess(
            NetStarsImportHistory history, ImportCounts counts, String lostWarning) {
        history.setStatus(NetStarsImportHistory.STATUS_SUCCESS);
        history.setFetchedCount(counts.fetched());
        history.setInsertedCount(counts.inserted());
        history.setUpdatedCount(counts.updated());
        history.setPageCount(counts.pageCount());
        if (lostWarning != null && !lostWarning.isEmpty()) {
            history.setErrorMessage(lostWarning);
        }
        history.setFinishedAt(LocalDateTime.now(clock));
        historyRepository.save(history);
        log.info("還元データ取込 完了: 取得 {} 件 / 新規 {} 件 / 更新 {} 件 (期間 {}〜{})",
                counts.fetched(), counts.inserted(), counts.updated(),
                history.getBeginDate(), history.getEndDate());
        String message = "取得 " + counts.fetched() + " 件（新規 " + counts.inserted()
                + " 件 / 更新 " + counts.updated() + " 件）を取り込みました。";
        if (lostWarning != null && !lostWarning.isEmpty()) {
            message += " " + lostWarning;
        }
        return toResult(history, message);
    }

    private NetStarsImportResult finishSkipped(
            NetStarsImportHistory history, String message) {
        history.setStatus(NetStarsImportHistory.STATUS_SKIPPED);
        history.setErrorMessage(message);
        history.setFinishedAt(LocalDateTime.now(clock));
        historyRepository.save(history);
        return toResult(history, message);
    }

    private NetStarsImportResult finishFailed(
            NetStarsImportHistory history, String message) {
        history.setStatus(NetStarsImportHistory.STATUS_FAILED);
        history.setErrorMessage(message);
        history.setFinishedAt(LocalDateTime.now(clock));
        historyRepository.save(history);
        return toResult(history,
                "取込に失敗しました。" + (message == null ? "" : message));
    }

    private NetStarsImportResult toResult(
            NetStarsImportHistory history, String message) {
        return new NetStarsImportResult(
                history.getId(),
                history.getStatus(),
                history.getBeginDate(),
                history.getEndDate(),
                history.getFetchedCount(),
                history.getInsertedCount(),
                history.getUpdatedCount(),
                history.getPageCount(),
                message);
    }

    private NetStarsImportHistoryListItemDto toListItem(
            NetStarsImportHistory history) {
        NetStarsImportHistoryListItemDto dto = new NetStarsImportHistoryListItemDto();
        dto.setId(history.getId());
        dto.setImportTypeLabel(importTypeLabel(history.getImportType()));
        dto.setStatusCode(history.getStatus());
        dto.setStatusLabel(statusLabel(history.getStatus()));
        dto.setTargetPeriod(formatPeriod(
                history.getBeginDate(), history.getEndDate()));
        dto.setFetchedCount(history.getFetchedCount());
        dto.setInsertedCount(history.getInsertedCount());
        dto.setUpdatedCount(history.getUpdatedCount());
        dto.setStartedAt(formatDateTime(history.getStartedAt()));
        dto.setFinishedAt(formatDateTime(history.getFinishedAt()));
        dto.setErrorMessage(history.getErrorMessage() == null
                ? "" : history.getErrorMessage());
        return dto;
    }

    private String importTypeLabel(String importType) {
        return switch (importType == null ? "" : importType) {
            case NetStarsImportHistory.IMPORT_TYPE_SCHEDULED -> "自動";
            case NetStarsImportHistory.IMPORT_TYPE_RESTART -> "手動再開";
            case NetStarsImportHistory.IMPORT_TYPE_AUTO_RESUME -> "自動再開";
            default -> importType == null ? "" : importType;
        };
    }

    private String statusLabel(String status) {
        return switch (status == null ? "" : status) {
            case NetStarsImportHistory.STATUS_RUNNING -> "実行中";
            case NetStarsImportHistory.STATUS_SUCCESS -> "成功";
            case NetStarsImportHistory.STATUS_FAILED -> "失敗";
            case NetStarsImportHistory.STATUS_SKIPPED -> "スキップ";
            default -> status == null ? "" : status;
        };
    }

    private String formatPeriod(LocalDate beginDate, LocalDate endDate) {
        if (beginDate == null || endDate == null) {
            return "";
        }
        String begin = DISPLAY_DATE_FORMAT.format(beginDate);
        if (beginDate.equals(endDate)) {
            return begin;
        }
        return begin + " 〜 " + DISPLAY_DATE_FORMAT.format(endDate);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DISPLAY_DATE_TIME_FORMAT.format(value);
    }

    /**
     * 取込 1 回分の集計値。
     *
     * @param fetched 取得件数
     * @param inserted 新規登録件数
     * @param updated 更新件数
     * @param pageCount 取得ページ数
     */
    private record ImportCounts(
            int fetched, int inserted, int updated, int pageCount) {
    }
}
