package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantApplicationDocumentRepository
        extends JpaRepository<MerchantApplicationDocument, Long> {
}
