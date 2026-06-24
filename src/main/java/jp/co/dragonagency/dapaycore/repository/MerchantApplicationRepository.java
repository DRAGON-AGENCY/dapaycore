package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MerchantApplicationRepository
        extends JpaRepository<MerchantApplication, String> {

    List<MerchantApplication> findByContactEmailAndDeleteFlagFalse(String contactEmail);

    @Query("SELECT a FROM MerchantApplication a WHERE a.deleteFlag = false ORDER BY a.submittedAt DESC NULLS LAST")
    List<MerchantApplication> findAllOrderBySubmittedAtDesc();

    Optional<MerchantApplication> findByMemberCodeAndDeleteFlagFalse(String memberCode);
}
