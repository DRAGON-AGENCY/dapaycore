package jp.co.dragonagency.dapaycore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.dragonagency.dapaycore.model.ContactInquiry;

/**
 * お問い合わせ（m_contact_inquiry）の永続化を担うリポジトリ。
 * 主キーはお問い合わせ番号（inquiry_number）を表す String 型とする。
 */
@Repository
public interface ContactInquiryRepository
        extends JpaRepository<ContactInquiry, String> {

    /**
     * 全お問い合わせを受付日時の降順で取得する。
     *
     * @return お問い合わせの一覧
     */
    List<ContactInquiry> findAllByOrderByCreatedAtDesc();

    /**
     * お問い合わせ番号が最大のレコードを 1 件取得する。
     * 新規登録時の自動採番に使用する。
     *
     * @return お問い合わせ番号が最大のレコード。存在しない場合は空
     */
    Optional<ContactInquiry> findFirstByOrderByInquiryNumberDesc();
}
