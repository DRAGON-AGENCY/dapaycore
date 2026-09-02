package jp.co.dragonagency.dapaycore.dto;

/**
 * 還元データ取込画面に表示する受信稼働状態。
 *
 * @param enabled          受信が稼働中なら true
 * @param apiConfigured    API 接続情報が設定済みなら true
 * @param autoResumed      直近の再開が自動（規定日数経過）だったなら true
 * @param stoppedAtText    停止した日時（yyyy/MM/dd HH:mm）。稼働中は空文字
 * @param autoResumeText   自動再開の予定日（yyyy/MM/dd 頃）。稼働中は空文字
 */
public record NetStarsImportControlView(
        boolean enabled,
        boolean apiConfigured,
        boolean autoResumed,
        String stoppedAtText,
        String autoResumeText) {
}
