package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MerchantApplicationDocumentRepository
        extends JpaRepository<MerchantApplicationDocument, Long> {

    List<MerchantApplicationDocument> findByMemberCodeAndDeleteFlagFalse(String memberCode);

    @Modifying
    @Query("UPDATE MerchantApplicationDocument d SET d.deleteFlag = true WHERE d.memberCode = :memberCode")
    void logicalDeleteByMemberCode(@Param("memberCode") String memberCode);
}
