package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.FeeRateListItemDto;
import jp.co.dragonagency.dapaycore.dto.FeeRateRequest;
import jp.co.dragonagency.dapaycore.dto.FeeRateResponse;
import jp.co.dragonagency.dapaycore.model.FeeRate;
import jp.co.dragonagency.dapaycore.repository.FeeRateRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 手数料レートの一覧取得・登録・更新・削除を担うサービス。
 */
@Service
public class FeeRateService {

    private static final String SQL_FIND_ALL =
            "SELECT f.id, f.member_code, a.corporate_name_kana, "
            + "f.start_date, f.end_date, f.fee_rate, "
            + "CASE "
            + "  WHEN f.end_date IS NOT NULL AND f.end_date < CURRENT_DATE THEN 'expired' "
            + "  WHEN f.start_date > CURRENT_DATE THEN 'future' "
            + "  ELSE 'valid' "
            + "END AS status "
            + "FROM m_fee_rate f "
            + "LEFT JOIN m_merchant_application a ON a.member_code = f.member_code "
            + "WHERE f.delete_flag = false "
            + "ORDER BY f.member_code, f.start_date";

    private final FeeRateRepository feeRateRepository;
    private final MerchantApplicationRepository applicationRepository;
    private final JdbcTemplate jdbcTemplate;

    public FeeRateService(
            FeeRateRepository feeRateRepository,
            MerchantApplicationRepository applicationRepository,
            JdbcTemplate jdbcTemplate) {
        this.feeRateRepository = feeRateRepository;
        this.applicationRepository = applicationRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<FeeRateListItemDto> findAllForList() {
        return jdbcTemplate.query(SQL_FIND_ALL, (rs, rowNum) -> {
            FeeRateListItemDto dto = new FeeRateListItemDto();
            dto.setId(rs.getLong("id"));
            dto.setMemberCode(rs.getString("member_code"));
            dto.setCorporateNameKana(
                Objects.requireNonNullElse(rs.getString("corporate_name_kana"), ""));
            dto.setStartDate(rs.getDate("start_date").toLocalDate().toString());
            Date endDate = rs.getDate("end_date");
            dto.setEndDate(endDate != null ? endDate.toLocalDate().toString() : null);
            BigDecimal rate = rs.getBigDecimal("fee_rate");
            dto.setFeeRateDisplay(
                rate.setScale(2, RoundingMode.HALF_UP).toPlainString());
            dto.setStatus(rs.getString("status"));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public FeeRateResponse findById(long id) {
        FeeRate e = feeRateRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "手数料レートが見つかりません: " + id));
        return toResponse(e);
    }

    @Transactional
    public FeeRateResponse create(FeeRateRequest req) {
        validate(req, -1L);
        FeeRate e = new FeeRate();
        applyRequest(e, req);
        return toResponse(feeRateRepository.save(e));
    }

    @Transactional
    public FeeRateResponse update(long id, FeeRateRequest req) {
        FeeRate e = feeRateRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "手数料レートが見つかりません: " + id));
        validate(req, id);
        applyRequest(e, req);
        return toResponse(feeRateRepository.save(e));
    }

    @Transactional
    public void delete(long id) {
        FeeRate e = feeRateRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "手数料レートが見つかりません: " + id));
        e.setDeleteFlag(true);
    }

    @Transactional(readOnly = true)
    public String findMemberName(String memberCode) {
        if (memberCode == null || memberCode.isBlank()) {
            return null;
        }
        return applicationRepository.findByMemberCodeAndDeleteFlagFalse(memberCode)
                .map(a -> a.getCorporateNameKana())
                .orElse(null);
    }

    private void validate(FeeRateRequest req, long excludeId) {
        List<String> errors = new ArrayList<>();

        if (req.getMemberCode() == null || req.getMemberCode().isBlank()) {
            errors.add("会員コードは必須です");
        } else if (!applicationRepository
                .findByMemberCodeAndDeleteFlagFalse(req.getMemberCode().trim())
                .isPresent()) {
            errors.add("会員コードが見つかりません: " + req.getMemberCode());
        }

        LocalDate startDate = null;
        if (req.getStartDate() == null || req.getStartDate().isBlank()) {
            errors.add("適用開始日は必須です");
        } else {
            try {
                startDate = LocalDate.parse(req.getStartDate());
            } catch (DateTimeParseException ex) {
                errors.add("適用開始日の形式が正しくありません");
            }
        }

        LocalDate endDate = null;
        if (req.getEndDate() != null && !req.getEndDate().isBlank()) {
            try {
                endDate = LocalDate.parse(req.getEndDate());
            } catch (DateTimeParseException ex) {
                errors.add("適用終了日の形式が正しくありません");
            }
        }

        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            errors.add("適用終了日は適用開始日より後の日付を入力してください");
        }

        if (req.getFeeRate() == null) {
            errors.add("手数料率は必須です");
        } else if (req.getFeeRate().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("手数料率は0より大きい値を入力してください");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }

        if (startDate != null
                && req.getMemberCode() != null && !req.getMemberCode().isBlank()) {
            LocalDate finalStart = startDate;
            LocalDate finalEnd = endDate;
            boolean hasOverlap = feeRateRepository
                    .findActiveForOverlapCheck(req.getMemberCode().trim(), excludeId)
                    .stream()
                    .anyMatch(e -> overlaps(e.getStartDate(), e.getEndDate(),
                            finalStart, finalEnd));
            if (hasOverlap) {
                throw new IllegalArgumentException(
                        "同じ会員で期間が重複するレートがすでに登録されています");
            }
        }
    }

    private static boolean overlaps(
            LocalDate s1, LocalDate e1, LocalDate s2, LocalDate e2) {
        LocalDate eff1 = (e1 == null) ? LocalDate.MAX : e1;
        LocalDate eff2 = (e2 == null) ? LocalDate.MAX : e2;
        return !s1.isAfter(eff2) && !eff1.isBefore(s2);
    }

    private void applyRequest(FeeRate e, FeeRateRequest req) {
        e.setMemberCode(req.getMemberCode().trim());
        e.setStartDate(LocalDate.parse(req.getStartDate()));
        e.setEndDate((req.getEndDate() != null && !req.getEndDate().isBlank())
                ? LocalDate.parse(req.getEndDate()) : null);
        e.setFeeRate(req.getFeeRate());
    }

    private FeeRateResponse toResponse(FeeRate e) {
        FeeRateResponse r = new FeeRateResponse();
        r.setId(e.getId());
        r.setMemberCode(e.getMemberCode());
        r.setMemberName(findMemberName(e.getMemberCode()));
        r.setStartDate(e.getStartDate().toString());
        r.setEndDate(e.getEndDate() != null ? e.getEndDate().toString() : null);
        r.setFeeRate(e.getFeeRate());
        return r;
    }
}
