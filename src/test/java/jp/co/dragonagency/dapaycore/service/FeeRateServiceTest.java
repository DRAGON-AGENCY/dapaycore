package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.FeeRateRequest;
import jp.co.dragonagency.dapaycore.dto.FeeRateResponse;
import jp.co.dragonagency.dapaycore.model.FeeRate;
import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.repository.FeeRateRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeRateServiceTest {

    @Mock
    private FeeRateRepository feeRateRepository;

    @Mock
    private MerchantApplicationRepository applicationRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private FeeRateService service;

    // =========================================================
    // findById（項番11〜14）
    // =========================================================

    @Test
    void T11_findById_存在するidのときFeeRateResponseを返す() {
        FeeRate entity = buildFeeRate(9001L, "FE001",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("3.24"), false);
        when(feeRateRepository.findByIdAndDeleteFlagFalse(9001L))
                .thenReturn(Optional.of(entity));
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE001"))
                .thenReturn(Optional.of(buildApplication("FE001", "フィーテスト カブシキガイシャ")));

        FeeRateResponse result = service.findById(9001L);

        assertNotNull(result);
        assertEquals(9001L, result.getId());
        assertEquals("FE001", result.getMemberCode());
    }

    @Test
    void T12_findById_存在しないidのときIllegalArgumentExceptionをスロー() {
        when(feeRateRepository.findByIdAndDeleteFlagFalse(99999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.findById(99999L));

        assertTrue(ex.getMessage().contains("手数料レートが見つかりません"));
    }

    @Test
    void T13_findById_deleteFlagTrueのidのときIllegalArgumentExceptionをスロー() {
        when(feeRateRepository.findByIdAndDeleteFlagFalse(9004L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.findById(9004L));

        assertTrue(ex.getMessage().contains("手数料レートが見つかりません"));
    }

    @Test
    void T14_findById_レスポンスにfindMemberNameのmemberNameが含まれる() {
        FeeRate entity = buildFeeRate(9001L, "FE001",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("3.24"), false);
        when(feeRateRepository.findByIdAndDeleteFlagFalse(9001L))
                .thenReturn(Optional.of(entity));
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE001"))
                .thenReturn(Optional.of(buildApplication("FE001", "フィーテスト カブシキガイシャ")));

        FeeRateResponse result = service.findById(9001L);

        assertEquals("フィーテスト カブシキガイシャ", result.getMemberName());
    }

    // =========================================================
    // findMemberName（項番15〜19）
    // =========================================================

    @Test
    void T15_findMemberName_memberCodeがnullのときnullを返す() {
        assertNull(service.findMemberName(null));
    }

    @Test
    void T16_findMemberName_memberCodeが空文字のときnullを返す() {
        assertNull(service.findMemberName(""));
    }

    @Test
    void T17_findMemberName_deleteFlagFalseの会員が存在するときcorporateNameKanaを返す() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE001"))
                .thenReturn(Optional.of(buildApplication("FE001", "フィーテスト カブシキガイシャ")));

        assertEquals("フィーテスト カブシキガイシャ", service.findMemberName("FE001"));
    }

    @Test
    void T18_findMemberName_deleteFlagTrueの会員のみのときnullを返す() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE003"))
                .thenReturn(Optional.empty());

        assertNull(service.findMemberName("FE003"));
    }

    @Test
    void T19_findMemberName_存在しないmemberCodeのときnullを返す() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("ZZZZ"))
                .thenReturn(Optional.empty());

        assertNull(service.findMemberName("ZZZZ"));
    }

    // =========================================================
    // create - 入力検証（項番20〜32）
    // =========================================================

    @Test
    void T20_create_memberCodeがnullのとき会員コードは必須ですエラーが含まれる() {
        FeeRateRequest req = buildRequest(null, "2026-01-01", null,
                new BigDecimal("3.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("会員コードは必須です"));
    }

    @Test
    void T21_create_memberCodeが空文字のとき会員コードは必須ですエラーが含まれる() {
        FeeRateRequest req = buildRequest("", "2026-01-01", null,
                new BigDecimal("3.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("会員コードは必須です"));
    }

    @Test
    void T22_create_存在しないmemberCodeのとき会員コードが見つかりませんエラーが含まれる() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("ZZZZ"))
                .thenReturn(Optional.empty());
        FeeRateRequest req = buildRequest("ZZZZ", "2026-01-01", null,
                new BigDecimal("3.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("会員コードが見つかりません"));
    }

    @Test
    void T23_create_startDateが空のとき適用開始日は必須ですエラーが含まれる() {
        FeeRateRequest req = buildRequest("FE001", "", null, new BigDecimal("3.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("適用開始日は必須です"));
    }

    @Test
    void T24_create_startDateが不正形式のとき適用開始日の形式が正しくありませんエラーが含まれる() {
        FeeRateRequest req = buildRequest("FE001", "20260101", null,
                new BigDecimal("3.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("適用開始日の形式が正しくありません"));
    }

    @Test
    void T25_create_endDateが不正形式のとき適用終了日の形式が正しくありませんエラーが含まれる() {
        FeeRateRequest req = buildRequest("FE001", "2026-01-01", "20261231",
                new BigDecimal("3.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("適用終了日の形式が正しくありません"));
    }

    @Test
    void T26_create_endDateがstartDate以前のとき適用終了日はエラーが含まれる() {
        FeeRateRequest req = buildRequest("FE001", "2026-06-01", "2026-06-01",
                new BigDecimal("3.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("適用終了日は適用開始日より後の日付を入力してください"));
    }

    @Test
    void T27_create_endDateがnullのとき終了日チェックはスキップされる() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE001"))
                .thenReturn(Optional.of(buildApplication("FE001", "テスト")));
        when(feeRateRepository.findActiveForOverlapCheck("FE001", -1L))
                .thenReturn(Collections.emptyList());
        when(feeRateRepository.save(any(FeeRate.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        FeeRateRequest req = buildRequest("FE001", "2026-01-01", null,
                new BigDecimal("3.00"));

        FeeRateResponse result = service.create(req);

        assertNotNull(result);
        assertNull(result.getEndDate());
    }

    @Test
    void T28_create_feeRateがnullのとき手数料率は必須ですエラーが含まれる() {
        FeeRateRequest req = buildRequest("FE001", "2026-01-01", null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("手数料率は必須です"));
    }

    @Test
    void T29_create_feeRateが0のとき手数料率は0より大きい値エラーが含まれる() {
        FeeRateRequest req = buildRequest("FE001", "2026-01-01", null,
                BigDecimal.ZERO);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("手数料率は0より大きい値を入力してください"));
    }

    @Test
    void T30_create_feeRateが負値のとき手数料率は0より大きい値エラーが含まれる() {
        FeeRateRequest req = buildRequest("FE001", "2026-01-01", null,
                new BigDecimal("-1.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("手数料率は0より大きい値を入力してください"));
    }

    @Test
    void T31_create_同一会員で期間が重複する場合重複エラーが発生する() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE004"))
                .thenReturn(Optional.of(buildApplication("FE004", "重複テスト")));
        FeeRate existing = buildFeeRate(9007L, "FE004",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("3.50"), false);
        when(feeRateRepository.findActiveForOverlapCheck("FE004", -1L))
                .thenReturn(List.of(existing));
        FeeRateRequest req = buildRequest("FE004", "2026-06-01", "2026-09-30",
                new BigDecimal("3.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("同じ会員で期間が重複するレートがすでに登録されています"));
    }

    @Test
    void T32_create_複数のバリデーションエラーは改行区切りで含まれる() {
        FeeRateRequest req = buildRequest(null, null, null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req));

        assertTrue(ex.getMessage().contains("会員コードは必須です"));
        assertTrue(ex.getMessage().contains("適用開始日は必須です"));
        assertTrue(ex.getMessage().contains("手数料率は必須です"));
        assertTrue(ex.getMessage().contains("\n"));
    }

    // =========================================================
    // create - 登録処理（項番33）
    // =========================================================

    @Test
    void T33_create_全て有効なリクエストのとき新規FeeRateが保存されFeeRateResponseを返す() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE001"))
                .thenReturn(Optional.of(buildApplication("FE001", "テスト")));
        when(feeRateRepository.findActiveForOverlapCheck("FE001", -1L))
                .thenReturn(Collections.emptyList());
        when(feeRateRepository.save(any(FeeRate.class)))
                .thenAnswer(inv -> {
                    FeeRate e = inv.getArgument(0);
                    e.setId(9001L);
                    return e;
                });
        FeeRateRequest req = buildRequest("FE001", "2026-01-01", "2026-12-31",
                new BigDecimal("3.24"));

        FeeRateResponse result = service.create(req);

        assertNotNull(result);
        assertEquals("FE001", result.getMemberCode());
        assertEquals("2026-01-01", result.getStartDate());
        verify(feeRateRepository).save(any(FeeRate.class));
    }

    // =========================================================
    // update（項番34〜37）
    // =========================================================

    @Test
    void T34_update_存在しないidのときIllegalArgumentExceptionをスロー() {
        when(feeRateRepository.findByIdAndDeleteFlagFalse(99999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.update(99999L, buildRequest("FE001", "2026-01-01",
                        null, new BigDecimal("3.00"))));

        assertTrue(ex.getMessage().contains("手数料レートが見つかりません"));
    }

    @Test
    void T35_update_deleteFlagTrueのidのときIllegalArgumentExceptionをスロー() {
        when(feeRateRepository.findByIdAndDeleteFlagFalse(9004L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.update(9004L, buildRequest("FE001", "2026-01-01",
                        null, new BigDecimal("3.00"))));

        assertTrue(ex.getMessage().contains("手数料レートが見つかりません"));
    }

    @Test
    void T36_update_全て有効なリクエストのときFeeRateが更新されFeeRateResponseを返す() {
        FeeRate entity = buildFeeRate(9001L, "FE001",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("3.24"), false);
        when(feeRateRepository.findByIdAndDeleteFlagFalse(9001L))
                .thenReturn(Optional.of(entity));
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE001"))
                .thenReturn(Optional.of(buildApplication("FE001", "テスト")));
        when(feeRateRepository.findActiveForOverlapCheck("FE001", 9001L))
                .thenReturn(Collections.emptyList());
        when(feeRateRepository.save(any(FeeRate.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        FeeRateRequest req = buildRequest("FE001", "2026-03-01", "2026-09-30",
                new BigDecimal("2.50"));

        FeeRateResponse result = service.update(9001L, req);

        assertNotNull(result);
        assertEquals("2026-03-01", result.getStartDate());
    }

    @Test
    void T37_update_自身は期間重複チェックから除外される() {
        FeeRate entity = buildFeeRate(9001L, "FE001",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("3.24"), false);
        when(feeRateRepository.findByIdAndDeleteFlagFalse(9001L))
                .thenReturn(Optional.of(entity));
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE001"))
                .thenReturn(Optional.of(buildApplication("FE001", "テスト")));
        when(feeRateRepository.findActiveForOverlapCheck("FE001", 9001L))
                .thenReturn(Collections.emptyList());
        when(feeRateRepository.save(any(FeeRate.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        FeeRateRequest req = buildRequest("FE001", "2026-01-01", "2026-12-31",
                new BigDecimal("3.24"));

        service.update(9001L, req);

        verify(feeRateRepository).findActiveForOverlapCheck("FE001", 9001L);
    }

    // =========================================================
    // 期間重複チェック（項番38〜41）
    // =========================================================

    @Test
    void T38_create_新規期間が既存期間に完全包含される場合は重複と判定される() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE004"))
                .thenReturn(Optional.of(buildApplication("FE004", "テスト")));
        FeeRate existing = buildFeeRate(9007L, "FE004",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("3.50"), false);
        when(feeRateRepository.findActiveForOverlapCheck("FE004", -1L))
                .thenReturn(List.of(existing));
        FeeRateRequest req = buildRequest("FE004", "2026-03-01", "2026-09-30",
                new BigDecimal("3.00"));

        assertThrows(IllegalArgumentException.class, () -> service.create(req));
    }

    @Test
    void T39_create_既存期間が新規期間に完全包含される場合は重複と判定される() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE004"))
                .thenReturn(Optional.of(buildApplication("FE004", "テスト")));
        FeeRate existing = buildFeeRate(9007L, "FE004",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30),
                new BigDecimal("3.50"), false);
        when(feeRateRepository.findActiveForOverlapCheck("FE004", -1L))
                .thenReturn(List.of(existing));
        FeeRateRequest req = buildRequest("FE004", "2026-01-01", "2026-12-31",
                new BigDecimal("3.00"));

        assertThrows(IllegalArgumentException.class, () -> service.create(req));
    }

    @Test
    void T40_create_新規の開始日が既存の終了日より後の場合は重複なしと判定される() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE004"))
                .thenReturn(Optional.of(buildApplication("FE004", "テスト")));
        FeeRate existing = buildFeeRate(9007L, "FE004",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("3.50"), false);
        when(feeRateRepository.findActiveForOverlapCheck("FE004", -1L))
                .thenReturn(List.of(existing));
        when(feeRateRepository.save(any(FeeRate.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        FeeRateRequest req = buildRequest("FE004", "2027-01-01", null,
                new BigDecimal("3.00"));

        assertNotNull(service.create(req));
    }

    @Test
    void T41_create_既存の終了日がnull無期限のとき新規と常に重複と判定される() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("FE004"))
                .thenReturn(Optional.of(buildApplication("FE004", "テスト")));
        FeeRate existing = buildFeeRate(9009L, "FE004",
                LocalDate.of(2025, 1, 1), null,
                new BigDecimal("2.50"), false);
        when(feeRateRepository.findActiveForOverlapCheck("FE004", -1L))
                .thenReturn(List.of(existing));
        FeeRateRequest req = buildRequest("FE004", "2026-01-01", "2026-12-31",
                new BigDecimal("3.00"));

        assertThrows(IllegalArgumentException.class, () -> service.create(req));
    }

    // =========================================================
    // delete（項番42〜44）
    // =========================================================

    @Test
    void T42_delete_存在しないidのときIllegalArgumentExceptionをスロー() {
        when(feeRateRepository.findByIdAndDeleteFlagFalse(99999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.delete(99999L));

        assertTrue(ex.getMessage().contains("手数料レートが見つかりません"));
    }

    @Test
    void T43_delete_deleteFlagTrueのidのときIllegalArgumentExceptionをスロー() {
        when(feeRateRepository.findByIdAndDeleteFlagFalse(9004L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.delete(9004L));

        assertTrue(ex.getMessage().contains("手数料レートが見つかりません"));
    }

    @Test
    void T44_delete_正常なidのときdeleteFlagがtrueに設定される() {
        FeeRate entity = buildFeeRate(9001L, "FE001",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("3.24"), false);
        when(feeRateRepository.findByIdAndDeleteFlagFalse(9001L))
                .thenReturn(Optional.of(entity));

        service.delete(9001L);

        assertTrue(entity.isDeleteFlag());
    }

    // =========================================================
    // ヘルパーメソッド
    // =========================================================

    private static FeeRate buildFeeRate(long id, String memberCode,
            LocalDate startDate, LocalDate endDate,
            BigDecimal feeRate, boolean deleteFlag) {
        FeeRate e = new FeeRate();
        e.setId(id);
        e.setMemberCode(memberCode);
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setFeeRate(feeRate);
        e.setDeleteFlag(deleteFlag);
        return e;
    }

    private static MerchantApplication buildApplication(
            String memberCode, String kana) {
        MerchantApplication a = new MerchantApplication();
        a.setMemberCode(memberCode);
        a.setCorporateNameKana(kana);
        return a;
    }

    private static FeeRateRequest buildRequest(String memberCode,
            String startDate, String endDate, BigDecimal feeRate) {
        FeeRateRequest req = new FeeRateRequest();
        req.setMemberCode(memberCode);
        req.setStartDate(startDate);
        req.setEndDate(endDate);
        req.setFeeRate(feeRate);
        return req;
    }
}
