package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.TransferFeeListItemDto;
import jp.co.dragonagency.dapaycore.dto.TransferFeeRequest;
import jp.co.dragonagency.dapaycore.dto.TransferFeeResponse;
import jp.co.dragonagency.dapaycore.model.TransferFee;
import jp.co.dragonagency.dapaycore.repository.TransferFeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferFeeServiceTest {

    @Mock
    private TransferFeeRepository transferFeeRepository;

    @InjectMocks
    private TransferFeeService service;

    // =========================================================
    // findAllForList（項番05〜08）
    // =========================================================

    @Test
    void T05_findAllForList_DEFAULT行が先頭にソートされる() {
        TransferFee normal = buildTransferFee("0310", 200, false);
        TransferFee defaultRow = buildTransferFee(TransferFee.DEFAULT_BANK_CODE, 100, false);
        when(transferFeeRepository.findByDeleteFlagFalse())
                .thenReturn(List.of(normal, defaultRow));

        List<TransferFeeListItemDto> result = service.findAllForList();

        assertEquals(TransferFee.DEFAULT_BANK_CODE, result.get(0).getBankCode());
        assertEquals("0310", result.get(1).getBankCode());
    }

    @Test
    void T06_findAllForList_DEFAULT以外は銀行コード昇順にソートされる() {
        TransferFee bank0500 = buildTransferFee("0500", 100, false);
        TransferFee bank0310 = buildTransferFee("0310", 200, false);
        when(transferFeeRepository.findByDeleteFlagFalse())
                .thenReturn(List.of(bank0500, bank0310));

        List<TransferFeeListItemDto> result = service.findAllForList();

        assertEquals("0310", result.get(0).getBankCode());
        assertEquals("0500", result.get(1).getBankCode());
    }

    @Test
    void T07_findAllForList_deleteFlagFalseの行のみ取得する() {
        when(transferFeeRepository.findByDeleteFlagFalse())
                .thenReturn(List.of(buildTransferFee("0310", 200, false)));

        service.findAllForList();

        verify(transferFeeRepository).findByDeleteFlagFalse();
    }

    @Test
    void T08_findAllForList_データなしのとき空リストを返す() {
        when(transferFeeRepository.findByDeleteFlagFalse())
                .thenReturn(Collections.emptyList());

        List<TransferFeeListItemDto> result = service.findAllForList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================
    // findByBankCode（項番09〜10）
    // =========================================================

    @Test
    void T09_findByBankCode_存在するbankCodeのときTransferFeeResponseを返す() {
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("0310"))
                .thenReturn(Optional.of(buildTransferFee("0310", 200, false)));

        TransferFeeResponse result = service.findByBankCode("0310");

        assertNotNull(result);
        assertEquals("0310", result.getBankCode());
        assertEquals(200, result.getTransferFee());
    }

    @Test
    void T10_findByBankCode_存在しないbankCodeのときIllegalArgumentExceptionをスロー() {
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("9999"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.findByBankCode("9999"));

        assertTrue(ex.getMessage().contains("振込手数料が見つかりません"));
    }

    // =========================================================
    // create - 入力検証（項番11〜21）
    // =========================================================

    @Test
    void T11_create_bankCodeがnullのとき銀行コードは必須ですエラーが含まれる() {
        TransferFeeRequest req = buildRequest(null, 200, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req, "user001"));

        assertTrue(ex.getMessage().contains("銀行コードは必須です"));
    }

    @Test
    void T12_create_bankCodeが空文字のとき銀行コードは必須ですエラーが含まれる() {
        TransferFeeRequest req = buildRequest("", 200, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req, "user001"));

        assertTrue(ex.getMessage().contains("銀行コードは必須です"));
    }

    @Test
    void T13_create_bankCodeが数字4桁でもDEFAULTでもないとき形式エラーが含まれる() {
        TransferFeeRequest req = buildRequest("ABCD", 200, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req, "user001"));

        assertTrue(ex.getMessage().contains(
                "銀行コードは「DEFAULT」または数字4桁で入力してください"));
    }

    @Test
    void T14_create_bankCodeがDEFAULTのとき形式チェックをスキップする() {
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse(TransferFee.DEFAULT_BANK_CODE))
                .thenReturn(Optional.empty());
        when(transferFeeRepository.save(any(TransferFee.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        TransferFeeRequest req = buildRequest(TransferFee.DEFAULT_BANK_CODE, 0, null);

        TransferFeeResponse result = service.create(req, "user001");

        assertNotNull(result);
        assertEquals(TransferFee.DEFAULT_BANK_CODE, result.getBankCode());
    }

    @Test
    void T15_create_既に登録済みのbankCodeのとき重複エラーが含まれる() {
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("0310"))
                .thenReturn(Optional.of(buildTransferFee("0310", 200, false)));
        TransferFeeRequest req = buildRequest("0310", 300, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req, "user001"));

        assertTrue(ex.getMessage().contains("この銀行コードは既に登録されています"));
    }

    @Test
    void T16_create_bankCodeが小文字defaultのとき大文字化されDEFAULTとして登録される() {
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse(TransferFee.DEFAULT_BANK_CODE))
                .thenReturn(Optional.empty());
        when(transferFeeRepository.save(any(TransferFee.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        TransferFeeRequest req = buildRequest("default", 100, null);

        TransferFeeResponse result = service.create(req, "user001");

        assertEquals(TransferFee.DEFAULT_BANK_CODE, result.getBankCode());
    }

    @Test
    void T17_create_transferFeeがnullのとき振込手数料は必須ですエラーが含まれる() {
        TransferFeeRequest req = buildRequest("0310", null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req, "user001"));

        assertTrue(ex.getMessage().contains("振込手数料は必須です"));
    }

    @Test
    void T18_create_transferFeeが負値のとき範囲エラーが含まれる() {
        TransferFeeRequest req = buildRequest("0310", -1, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req, "user001"));

        assertTrue(ex.getMessage().contains("振込手数料は0以上999999以下の整数で入力してください"));
    }

    @Test
    void T19_create_transferFeeが999999を超えるとき範囲エラーが含まれる() {
        TransferFeeRequest req = buildRequest("0310", 1_000_000, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req, "user001"));

        assertTrue(ex.getMessage().contains("振込手数料は0以上999999以下の整数で入力してください"));
    }

    @Test
    void T20_create_remarksが500文字を超えるとき備考は500文字以内エラーが含まれる() {
        String tooLong = "あ".repeat(501);
        TransferFeeRequest req = buildRequest("0310", 200, tooLong);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req, "user001"));

        assertTrue(ex.getMessage().contains("備考は500文字以内で入力してください"));
    }

    @Test
    void T21_create_複数のバリデーションエラーは改行区切りで含まれる() {
        TransferFeeRequest req = buildRequest(null, null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req, "user001"));

        assertTrue(ex.getMessage().contains("銀行コードは必須です"));
        assertTrue(ex.getMessage().contains("振込手数料は必須です"));
        assertTrue(ex.getMessage().contains("\n"));
    }

    // =========================================================
    // create - 登録処理（項番22）
    // =========================================================

    @Test
    void T22_create_全て有効なリクエストのとき新規TransferFeeが保存されTransferFeeResponseを返す() {
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("0310"))
                .thenReturn(Optional.empty());
        when(transferFeeRepository.save(any(TransferFee.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        TransferFeeRequest req = buildRequest("0310", 200, "GMOあおぞらネット銀行");

        TransferFeeResponse result = service.create(req, "user001");

        assertNotNull(result);
        assertEquals("0310", result.getBankCode());
        assertEquals(200, result.getTransferFee());
        verify(transferFeeRepository).save(any(TransferFee.class));
    }

    // =========================================================
    // update（項番23〜26）
    // =========================================================

    @Test
    void T23_update_存在しないbankCodeのときIllegalArgumentExceptionをスロー() {
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("9999"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.update("9999", buildRequest("9999", 200, null), "user001"));

        assertTrue(ex.getMessage().contains("振込手数料が見つかりません"));
    }

    @Test
    void T24_update_transferFeeが範囲外のとき範囲エラーが含まれる() {
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("0310"))
                .thenReturn(Optional.of(buildTransferFee("0310", 200, false)));
        TransferFeeRequest req = buildRequest("0310", 1_000_000, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.update("0310", req, "user001"));

        assertTrue(ex.getMessage().contains("振込手数料は0以上999999以下の整数で入力してください"));
    }

    @Test
    void T25_update_全て有効なリクエストのときTransferFeeが更新されTransferFeeResponseを返す() {
        TransferFee entity = buildTransferFee("0310", 200, false);
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("0310"))
                .thenReturn(Optional.of(entity));
        when(transferFeeRepository.save(any(TransferFee.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        TransferFeeRequest req = buildRequest("0310", 350, "更新後の備考");

        TransferFeeResponse result = service.update("0310", req, "user001");

        assertNotNull(result);
        assertEquals(350, result.getTransferFee());
        assertEquals("更新後の備考", result.getRemarks());
    }

    @Test
    void T26_update_updateUserIdが更新される() {
        TransferFee entity = buildTransferFee("0310", 200, false);
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("0310"))
                .thenReturn(Optional.of(entity));
        when(transferFeeRepository.save(any(TransferFee.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        TransferFeeRequest req = buildRequest("0310", 200, null);

        service.update("0310", req, "user002");

        assertEquals("user002", entity.getUpdateUserId());
    }

    // =========================================================
    // delete（項番27〜28）
    // =========================================================

    @Test
    void T27_delete_存在しないbankCodeのときIllegalArgumentExceptionをスロー() {
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("9999"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.delete("9999"));

        assertTrue(ex.getMessage().contains("振込手数料が見つかりません"));
    }

    @Test
    void T28_delete_正常なbankCodeのときdeleteFlagがtrueに設定される() {
        TransferFee entity = buildTransferFee("0310", 200, false);
        when(transferFeeRepository.findByBankCodeAndDeleteFlagFalse("0310"))
                .thenReturn(Optional.of(entity));

        service.delete("0310");

        assertTrue(entity.isDeleteFlag());
    }

    // =========================================================
    // ヘルパーメソッド
    // =========================================================

    private static TransferFee buildTransferFee(String bankCode, int transferFee, boolean deleteFlag) {
        TransferFee e = new TransferFee();
        e.setBankCode(bankCode);
        e.setTransferFee(transferFee);
        e.setDeleteFlag(deleteFlag);
        e.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        e.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return e;
    }

    private static TransferFeeRequest buildRequest(String bankCode, Integer transferFee, String remarks) {
        TransferFeeRequest req = new TransferFeeRequest();
        req.setBankCode(bankCode);
        req.setTransferFee(transferFee);
        req.setRemarks(remarks);
        return req;
    }
}
