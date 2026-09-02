package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.NetStarsImportHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * ネットスターズ還元データ取込履歴の永続化を担うリポジトリ。
 */
public interface NetStarsImportHistoryRepository
        extends JpaRepository<NetStarsImportHistory, Long> {

    /**
     * 取込履歴を開始日時の新しい順に取得する。
     *
     * @param pageable 取得件数の指定
     * @return 取込履歴の一覧
     */
    List<NetStarsImportHistory> findAllByOrderByStartedAtDesc(Pageable pageable);

    /**
     * 指定ステータスのうち、対象期間終了日が最も新しい取込履歴を 1 件取得する。
     * 次回取込の開始日（前回成功日の翌日）を求めるために使用する。
     *
     * @param status ステータス
     * @return 該当する取込履歴。存在しない場合は空
     */
    Optional<NetStarsImportHistory> findTopByStatusOrderByEndDateDesc(String status);
}
