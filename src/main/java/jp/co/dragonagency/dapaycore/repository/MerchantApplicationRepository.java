package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantApplicationRepository
        extends JpaRepository<MerchantApplication, String> {
}
