package jp.co.dragonagency.dapaycore.repository;

import jp.co.dragonagency.dapaycore.model.FeeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeeRateRepository extends JpaRepository<FeeRate, Long> {

    Optional<FeeRate> findByIdAndDeleteFlagFalse(long id);

    @Query("SELECT f FROM FeeRate f WHERE f.memberCode = :memberCode AND f.deleteFlag = false AND f.id <> :excludeId")
    List<FeeRate> findActiveForOverlapCheck(
            @Param("memberCode") String memberCode,
            @Param("excludeId") long excludeId
    );
}
