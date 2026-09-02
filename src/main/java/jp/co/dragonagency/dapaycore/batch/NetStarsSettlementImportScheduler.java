package jp.co.dragonagency.dapaycore.batch;

import jp.co.dragonagency.dapaycore.dto.NetStarsImportResult;
import jp.co.dragonagency.dapaycore.service.NetStarsSettlementImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ネットスターズ還元データを日次で取り込むスケジューラ。
 * StarPay 還元データは毎日朝 9 時以降に仮確定するため、余裕を見て 10 時に実行する。
 * 取得できるのは前日分までで、当日のデータは取得できない。
 * 実行タイミングを跨いで Tomcat が停止していた場合の取りこぼしは、
 * 取得期間を「前回成功日の翌日」から算出することで次回実行時に補完する。
 */
@Component
public class NetStarsSettlementImportScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(NetStarsSettlementImportScheduler.class);

    private final NetStarsSettlementImportService importService;

    public NetStarsSettlementImportScheduler(
            NetStarsSettlementImportService importService) {
        this.importService = importService;
    }

    /**
     * 毎日 10:00（日本時間）に還元データ取込を実行する。
     */
    @Scheduled(cron = "${netstars.import.cron:0 0 10 * * *}", zone = "Asia/Tokyo")
    public void importDaily() {
        log.info("還元データ 日次取込を開始します。");
        try {
            NetStarsImportResult result = importService.runScheduledImport();
            log.info("還元データ 日次取込が終了しました。 status={} {}",
                    result.status(), result.message());
        } catch (Exception e) {
            // スケジューラのスレッドを止めないよう、ここで確実に握りつぶす。
            log.error("還元データ 日次取込で想定外のエラーが発生しました: {}",
                    e.getMessage(), e);
        }
    }
}
