package jp.co.dragonagency.dapaycore.dto;

/**
 * 還元データ取込画面の取込履歴一覧に表示する 1 行分のデータ。
 * 表示に必要な項目を文字列へ整形済みの状態で保持する。
 */
public class NetStarsImportHistoryListItemDto {

    private long id;
    private String importTypeLabel;
    private String statusLabel;
    private String statusCode;
    private String targetPeriod;
    private int fetchedCount;
    private int insertedCount;
    private int updatedCount;
    private String startedAt;
    private String finishedAt;
    private String errorMessage;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImportTypeLabel() {
        return importTypeLabel;
    }

    public void setImportTypeLabel(String importTypeLabel) {
        this.importTypeLabel = importTypeLabel;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getTargetPeriod() {
        return targetPeriod;
    }

    public void setTargetPeriod(String targetPeriod) {
        this.targetPeriod = targetPeriod;
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

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
