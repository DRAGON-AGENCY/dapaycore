package jp.co.dragonagency.dapaycore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * 日時の取得元となる {@link Clock} の定義。
 * 日付境界を扱う処理（還元データ取込の取得期間算出など）で
 * テスト時に時刻を固定できるよう、直接 {@code LocalDate.now()} を呼ばず
 * この Bean を注入して使用する。タイムゾーンは日本時間で固定する。
 */
@Configuration
public class ApplicationClockConfig {

    private static final ZoneId ZONE_TOKYO = ZoneId.of("Asia/Tokyo");

    @Bean
    public Clock clock() {
        return Clock.system(ZONE_TOKYO);
    }
}
