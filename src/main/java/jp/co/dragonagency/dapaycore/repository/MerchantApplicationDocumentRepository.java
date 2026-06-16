package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MerchantApplicationDocumentRepository
        extends JpaRepository<MerchantApplicationDocument, Long> {

    @Query("SELECT d.memberCode, COUNT(d) FROM MerchantApplicationDocument d GROUP BY d.memberCode")
    List<Object[]> countGroupByMemberCode();

    List<MerchantApplicationDocument> findByMemberCode(String memberCode);

    void deleteByMemberCode(String memberCode);
}
