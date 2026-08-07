package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.TransferFeeListItemDto;
import jp.co.dragonagency.dapaycore.dto.TransferFeeRequest;
import jp.co.dragonagency.dapaycore.dto.TransferFeeResponse;
import jp.co.dragonagency.dapaycore.model.TransferFee;
import jp.co.dragonagency.dapaycore.repository.TransferFeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 振込手数料の一覧取得・登録・更新・削除を担うサービス。
 */
@Service
public class TransferFeeService {

    private static final Pattern BANK_CODE_PATTERN = Pattern.compile("^[0-9]{4}$");
    private static final int TRANSFER_FEE_MIN = 0;
    private static final int TRANSFER_FEE_MAX = 999999;

    private final TransferFeeRepository transferFeeRepository;

    public TransferFeeService(TransferFeeRepository transferFeeRepository) {
        this.transferFeeRepository = transferFeeRepository;
    }

    @Transactional(readOnly = true)
    public List<TransferFeeListItemDto> findAllForList() {
        List<TransferFee> entities = new ArrayList<>(
                transferFeeRepository.findByDeleteFlagFalse());
        entities.sort(Comparator
                .comparing((TransferFee e) -> !TransferFee.DEFAULT_BANK_CODE.equals(e.getBankCode()))
                .thenComparing(TransferFee::getBankCode));
        List<TransferFeeListItemDto> list = new ArrayList<>();
        for (TransferFee e : entities) {
            list.add(toListItem(e));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public TransferFeeResponse findByBankCode(String bankCode) {
        TransferFee e = transferFeeRepository.findByBankCodeAndDeleteFlagFalse(bankCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "振込手数料が見つかりません: " + bankCode));
        return toResponse(e);
    }

    @Transactional
    public TransferFeeResponse create(TransferFeeRequest req, String updateUserId) {
        validate(req);
        String bankCode = req.getBankCode().trim().toUpperCase();
        if (transferFeeRepository.findByBankCodeAndDeleteFlagFalse(bankCode).isPresent()) {
            throw new IllegalArgumentException("この銀行コードは既に登録されています: " + bankCode);
        }
        TransferFee e = new TransferFee();
        e.setBankCode(bankCode);
        applyRequest(e, req, updateUserId);
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        return toResponse(transferFeeRepository.save(e));
    }

    @Transactional
    public TransferFeeResponse update(String bankCode, TransferFeeRequest req, String updateUserId) {
        TransferFee e = transferFeeRepository.findByBankCodeAndDeleteFlagFalse(bankCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "振込手数料が見つかりません: " + bankCode));
        validateFeeAndRemarks(req);
        applyRequest(e, req, updateUserId);
        e.setUpdatedAt(LocalDateTime.now());
        return toResponse(transferFeeRepository.save(e));
    }

    @Transactional
    public void delete(String bankCode) {
        TransferFee e = transferFeeRepository.findByBankCodeAndDeleteFlagFalse(bankCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "振込手数料が見つかりません: " + bankCode));
        e.setDeleteFlag(true);
    }

    private void validate(TransferFeeRequest req) {
        List<String> errors = new ArrayList<>();

        if (req.getBankCode() == null || req.getBankCode().isBlank()) {
            errors.add("銀行コードは必須です");
        } else {
            String bankCode = req.getBankCode().trim().toUpperCase();
            if (!TransferFee.DEFAULT_BANK_CODE.equals(bankCode)
                    && !BANK_CODE_PATTERN.matcher(bankCode).matches()) {
                errors.add("銀行コードは「DEFAULT」または数字4桁で入力してください");
            }
        }

        collectFeeAndRemarksErrors(req, errors);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }

    private void validateFeeAndRemarks(TransferFeeRequest req) {
        List<String> errors = new ArrayList<>();
        collectFeeAndRemarksErrors(req, errors);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }

    private void collectFeeAndRemarksErrors(TransferFeeRequest req, List<String> errors) {
        if (req.getTransferFee() == null) {
            errors.add("振込手数料は必須です");
        } else if (req.getTransferFee() < TRANSFER_FEE_MIN || req.getTransferFee() > TRANSFER_FEE_MAX) {
            errors.add("振込手数料は" + TRANSFER_FEE_MIN + "以上" + TRANSFER_FEE_MAX + "以下の整数で入力してください");
        }
        if (req.getRemarks() != null && req.getRemarks().length() > 500) {
            errors.add("備考は500文字以内で入力してください");
        }
    }

    private void applyRequest(TransferFee e, TransferFeeRequest req, String updateUserId) {
        e.setTransferFee(req.getTransferFee());
        e.setRemarks(req.getRemarks());
        e.setUpdateUserId(updateUserId);
    }

    private TransferFeeListItemDto toListItem(TransferFee e) {
        TransferFeeListItemDto dto = new TransferFeeListItemDto();
        dto.setBankCode(e.getBankCode());
        dto.setTransferFee(e.getTransferFee());
        dto.setRemarks(e.getRemarks());
        dto.setUpdateUserId(e.getUpdateUserId());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    private TransferFeeResponse toResponse(TransferFee e) {
        TransferFeeResponse r = new TransferFeeResponse();
        r.setBankCode(e.getBankCode());
        r.setTransferFee(e.getTransferFee());
        r.setRemarks(e.getRemarks());
        return r;
    }
}
