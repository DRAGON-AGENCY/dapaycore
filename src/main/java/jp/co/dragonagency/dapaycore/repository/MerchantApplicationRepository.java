package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MerchantApplicationRepository
        extends JpaRepository<MerchantApplication, String> {

    Optional<MerchantApplication> findByContactEmail(String contactEmail);

    @Query("SELECT a FROM MerchantApplication a ORDER BY a.submittedAt DESC NULLS LAST")
    List<MerchantApplication> findAllOrderBySubmittedAtDesc();
}
