package jp.co.dragonagency.dapaycore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ネットスターズ還元データ取込履歴を表すエンティティ。
 * m_netstars_import_history テーブルの 1 行に対応し、
 * 日次スケジューラおよび画面からの手動再取込の 1 回の実行を記録する。
 */
@Entity
@Table(name = "m_netstars_import_history")
public class NetStarsImportHistory {

    /** 日次スケジューラによる実行を表す取込種別。 */
    public static final String IMPORT_TYPE_SCHEDULED = "SCHEDULED";

    /** 画面の RESTART ボタンによる実行を表す取込種別。 */
    public static final String IMPORT_TYPE_RESTART = "RESTART";

    /** 停止規定日数の経過による自動再開での実行を表す取込種別。 */
    public static final String IMPORT_TYPE_AUTO_RESUME = "AUTO_RESUME";

    /** 実行中を表すステータス。 */
    public static final String STATUS_RUNNING = "RUNNING";

    /** 正常終了を表すステータス。 */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /** 異常終了を表すステータス。 */
    public static final String STATUS_FAILED = "FAILED";

    /** API 未設定などで接続せず終了したことを表すステータス。 */
    public static final String STATUS_SKIPPED = "SKIPPED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "import_type")
    private String importType;

    @Column(name = "status")
    private String status;

    @Column(name = "begin_date")
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "fetched_count")
    private int fetchedCount;

    @Column(name = "inserted_count")
    private int insertedCount;

    @Column(name = "updated_count")
    private int updatedCount;

    @Column(name = "page_count")
    private int pageCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getFetchedCount() {
        return fetchedCount;
    }

    public void setFetchedCount(int fetchedCount) {
        this.fetchedCount = fetchedCount;
    }

    public int getInsertedCount() {
        return insertedCount;
    }

    public void setInsertedCount(int insertedCount) {
        this.insertedCount = insertedCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    @Override
    public String toString() {
        return "NetStarsImportHistory{"
                + "id=" + id
                + ", importType=" + importType
                + ", status=" + status
                + ", beginDate=" + beginDate
                + ", endDate=" + endDate
                + "}";
    }
}
