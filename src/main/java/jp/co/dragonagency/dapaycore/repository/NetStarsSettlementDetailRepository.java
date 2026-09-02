package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.NetStarsSettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * ネットスターズ還元データ（取引明細）の永続化を担うリポジトリ。
 */
public interface NetStarsSettlementDetailRepository
        extends JpaRepository<NetStarsSettlementDetail, Long> {

    /**
     * 重複排除キーで取引明細を 1 件取得する。
     * 再取得ウィンドウ内での UPSERT 判定に使用する。
     *
     * @param dedupKey 重複排除キー
     * @return 該当する取引明細。存在しない場合は空
     */
    Optional<NetStarsSettlementDetail> findByDedupKey(String dedupKey);
}
