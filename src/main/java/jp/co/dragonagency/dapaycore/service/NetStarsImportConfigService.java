package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.model.NetStarsImportConfig;
import jp.co.dragonagency.dapaycore.repository.NetStarsImportConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * ネットスターズ還元データ日次取込の稼働フラグ（STOP / RESTART / 自動再開）を管理するサービス。
 * フラグの更新をページ処理や取込本体とは独立したトランザクションで行うため、
 * 取込サービスから分離している。
 */
@Service
public class NetStarsImportConfigService {

    private static final Logger log =
            LoggerFactory.getLogger(NetStarsImportConfigService.class);

    private final NetStarsImportConfigRepository configRepository;
    private final Clock clock;

    public NetStarsImportConfigService(
            NetStarsImportConfigRepository configRepository, Clock clock) {
        this.configRepository = configRepository;
        this.clock = clock;
    }

    /**
     * 稼働設定を取得する。行が存在しない場合は稼働（enabled=true）の既定値を保存して返す。
     *
     * @return 稼働設定
     */
    @Transactional
    public NetStarsImportConfig getOrCreate() {
        return configRepository
                .findById(NetStarsImportConfig.KEY_DAILY_IMPORT)
                .orElseGet(this::createDefault);
    }

    /**
     * 日次取込が稼働中かどうかを返す。
     *
     * @return 稼働中の場合は true
     */
    @Transactional
    public boolean isEnabled() {
        return getOrCreate().isEnabled();
    }

    /**
     * 受信を停止する（STOP ボタン）。
     *
     * @param userId 操作した運用担当者の社員番号
     */
    @Transactional
    public void stop(String userId) {
        NetStarsImportConfig config = getOrCreate();
        config.setEnabled(false);
        config.setStoppedAt(LocalDateTime.now(clock));
        config.setAutoResumed(false);
        config.setNote("画面から受信を停止");
        touch(config, userId);
        configRepository.save(config);
        log.warn("還元データ受信を停止しました。操作者={}", userId);
    }

    /**
     * 受信を再開する（RESTART ボタン）。
     *
     * @param userId 操作した運用担当者の社員番号
     */
    @Transactional
    public void restart(String userId) {
        NetStarsImportConfig config = getOrCreate();
        config.setEnabled(true);
        config.setStoppedAt(null);
        config.setAutoResumed(false);
        config.setNote("画面から受信を再開");
        touch(config, userId);
        configRepository.save(config);
        log.warn("還元データ受信を再開しました。操作者={}", userId);
    }

    /**
     * 停止から一定日数が経過したことによる自動再開を記録する。
     */
    @Transactional
    public void markAutoResumed() {
        NetStarsImportConfig config = getOrCreate();
        config.setEnabled(true);
        config.setStoppedAt(null);
        config.setAutoResumed(true);
        config.setNote("停止から規定日数が経過したため自動再開");
        touch(config, null);
        configRepository.save(config);
        log.warn("還元データ受信が停止規定日数の経過により自動再開しました。");
    }

    private NetStarsImportConfig createDefault() {
        NetStarsImportConfig config = new NetStarsImportConfig();
        config.setConfigKey(NetStarsImportConfig.KEY_DAILY_IMPORT);
        config.setEnabled(true);
        config.setAutoResumed(false);
        config.setNote("還元データ日次取込の稼働フラグ");
        touch(config, null);
        return configRepository.save(config);
    }

    private void touch(NetStarsImportConfig config, String userId) {
        config.setUpdatedAt(LocalDateTime.now(clock));
        config.setUpdateUserId(userId);
    }
}
