package jp.co.dragonagency.dapaycore.dto;

import java.time.LocalDate;

/**
 * ネットスターズ還元データ取込 1 回分の実行結果を表す値オブジェクト。
 * 画面（手動再取込）および呼び出し元へ返却する。
 *
 * @param historyId     取込履歴レコードの id
 * @param status        ステータス（SUCCESS / FAILED / SKIPPED）
 * @param beginDate     取得対象期間の開始日
 * @param endDate       取得対象期間の終了日
 * @param fetchedCount  API から取得した明細行数
 * @param insertedCount 新規登録した明細行数
 * @param updatedCount  更新（上書き）した明細行数
 * @param pageCount     取得したページ数
 * @param message       利用者向けメッセージ（エラー時はエラー内容）
 */
public record NetStarsImportResult(
        long historyId,
        String status,
        LocalDate beginDate,
        LocalDate endDate,
        int fetchedCount,
        int insertedCount,
        int updatedCount,
        int pageCount,
        String message) {
}
