package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.NetStarsImportConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ネットスターズ還元データ日次取込の稼働設定の永続化を担うリポジトリ。
 */
public interface NetStarsImportConfigRepository
        extends JpaRepository<NetStarsImportConfig, String> {
}
