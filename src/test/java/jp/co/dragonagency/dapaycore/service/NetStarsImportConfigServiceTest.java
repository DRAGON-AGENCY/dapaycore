package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.model.NetStarsImportConfig;
import jp.co.dragonagency.dapaycore.repository.NetStarsImportConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link NetStarsImportConfigService} の単体テスト。
 * 単体テスト仕様書_還元データ取込履歴照会_自社mPOSキャッシュレス管理システム_v1.00.xlsx の 項番 T22〜T25（■ テストケース一覧）に対応する。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NetStarsImportConfigServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-09-02T05:00:00Z"), ZoneId.of("Asia/Tokyo"));

    @Mock
    private NetStarsImportConfigRepository configRepository;

    private NetStarsImportConfigService service;

    @BeforeEach
    void setUp() {
        service = new NetStarsImportConfigService(configRepository, FIXED_CLOCK);
        when(configRepository.save(any(NetStarsImportConfig.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void T22_getOrCreate_行が無いとき稼働の既定値を作成して返す() {
        when(configRepository.findById(NetStarsImportConfig.KEY_DAILY_IMPORT))
                .thenReturn(Optional.empty());

        NetStarsImportConfig config = service.getOrCreate();

        assertEquals(NetStarsImportConfig.KEY_DAILY_IMPORT, config.getConfigKey());
        assertTrue(config.isEnabled());
    }

    @Test
    void T23_stop_enabledをfalseにしstoppedAtを設定する() {
        when(configRepository.findById(NetStarsImportConfig.KEY_DAILY_IMPORT))
                .thenReturn(Optional.of(enabledConfig()));

        service.stop("user001");

        NetStarsImportConfig saved = captureSaved();
        assertFalse(saved.isEnabled());
        assertEquals(LocalDateTime.of(2026, 9, 2, 14, 0), saved.getStoppedAt());
        assertEquals("user001", saved.getUpdateUserId());
    }

    @Test
    void T24_restart_enabledをtrueに戻しstoppedAtをクリアする() {
        NetStarsImportConfig stopped = enabledConfig();
        stopped.setEnabled(false);
        stopped.setStoppedAt(LocalDateTime.of(2026, 8, 30, 9, 0));
        when(configRepository.findById(NetStarsImportConfig.KEY_DAILY_IMPORT))
                .thenReturn(Optional.of(stopped));

        service.restart("user002");

        NetStarsImportConfig saved = captureSaved();
        assertTrue(saved.isEnabled());
        assertNull(saved.getStoppedAt());
        assertFalse(saved.isAutoResumed());
    }

    @Test
    void T25_markAutoResumed_enabledをtrueに戻しautoResumedを立てる() {
        NetStarsImportConfig stopped = enabledConfig();
        stopped.setEnabled(false);
        stopped.setStoppedAt(LocalDateTime.of(2026, 8, 25, 9, 0));
        when(configRepository.findById(NetStarsImportConfig.KEY_DAILY_IMPORT))
                .thenReturn(Optional.of(stopped));

        service.markAutoResumed();

        NetStarsImportConfig saved = captureSaved();
        assertTrue(saved.isEnabled());
        assertNull(saved.getStoppedAt());
        assertTrue(saved.isAutoResumed());
    }

    private NetStarsImportConfig captureSaved() {
        org.mockito.ArgumentCaptor<NetStarsImportConfig> captor =
                org.mockito.ArgumentCaptor.forClass(NetStarsImportConfig.class);
        org.mockito.Mockito.verify(configRepository, org.mockito.Mockito.atLeastOnce())
                .save(captor.capture());
        return captor.getValue();
    }

    private static NetStarsImportConfig enabledConfig() {
        NetStarsImportConfig config = new NetStarsImportConfig();
        config.setConfigKey(NetStarsImportConfig.KEY_DAILY_IMPORT);
        config.setEnabled(true);
        return config;
    }
}
