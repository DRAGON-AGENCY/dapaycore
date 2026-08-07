package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.TransferFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferFeeRepository extends JpaRepository<TransferFee, String> {

    List<TransferFee> findByDeleteFlagFalse();

    Optional<TransferFee> findByBankCodeAndDeleteFlagFalse(String bankCode);
}
