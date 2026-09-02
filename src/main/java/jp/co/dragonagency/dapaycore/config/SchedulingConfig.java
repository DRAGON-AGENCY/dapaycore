package jp.co.dragonagency.dapaycore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * アプリケーション内でのスケジュール実行（{@code @Scheduled}）と
 * 非同期実行（{@code @Async}）を有効化する設定。
 * 外部 Tomcat 上で WAR が起動している間、登録されたスケジュールタスクが動作する。
 * RESTART 操作に伴う取込は HTTP 応答をブロックしないよう非同期で実行する。
 */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {
}
