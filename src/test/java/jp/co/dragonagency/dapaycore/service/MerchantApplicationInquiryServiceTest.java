package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationDocumentRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MerchantApplicationInquiryService の単体テスト。
 * 単体テスト仕様書_申込内容照会画面_v1.00.xlsx の項番08〜75に対応する。
 */
@ExtendWith(MockitoExtension.class)
class MerchantApplicationInquiryServiceTest {

    private static final String MEMBER_CODE = "TEST0001";

    @Mock
    private MerchantApplicationRepository applicationRepository;

    @Mock
    private MerchantApplicationDocumentRepository documentRepository;

    @InjectMocks
    private MerchantApplicationInquiryService service;

    // =========================================================
    // findApplication（項番08〜13）
    // =========================================================

    @Test
    void T08_findApplication_memberCodeがnullのときnullを返す() {
        assertNull(service.findApplication(null));
    }

    @Test
    void T09_findApplication_memberCodeが空文字のときnullを返す() {
        assertNull(service.findApplication(""));
    }

    @Test
    void T10_findApplication_memberCodeが空白のみのときnullを返す() {
        assertNull(service.findApplication("   "));
    }

    @Test
    void T11_findApplication_deleteFlagFalseで存在するレコードのときMerchantApplicationを返す() {
        MerchantApplication entity = new MerchantApplication();
        entity.setMemberCode(MEMBER_CODE);
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(entity));

        MerchantApplication result = service.findApplication(MEMBER_CODE);

        assertEquals(entity, result);
    }

    @Test
    void T12_findApplication_deleteFlagTrueのレコードのみのときnullを返す() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.empty());

        assertNull(service.findApplication(MEMBER_CODE));
    }

    @Test
    void T13_findApplication_存在しないmemberCodeのときnullを返す() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse("NOTFOUND"))
                .thenReturn(Optional.empty());

        assertNull(service.findApplication("NOTFOUND"));
    }

    // =========================================================
    // findDocumentMap（項番14〜19）
    // =========================================================

    @Test
    void T14_findDocumentMap_memberCodeがnullのとき空のMapを返す() {
        assertTrue(service.findDocumentMap(null).isEmpty());
    }

    @Test
    void T15_findDocumentMap_memberCodeが空文字のとき空のMapを返す() {
        assertTrue(service.findDocumentMap("").isEmpty());
    }

    @Test
    void T16_findDocumentMap_deleteFlagFalseの書類が存在するときdocumentTypeをキーとしたMapを返す() {
        MerchantApplicationDocument permit = buildDocument(
                MerchantApplicationDocument.TYPE_BUSINESS_PERMIT, "permit.pdf");
        MerchantApplicationDocument idFront = buildDocument(
                MerchantApplicationDocument.TYPE_ID_FRONT, "id_front.jpg");
        when(documentRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(List.of(permit, idFront));

        Map<String, MerchantApplicationDocument> result = service.findDocumentMap(MEMBER_CODE);

        assertEquals(2, result.size());
        assertEquals(permit, result.get(MerchantApplicationDocument.TYPE_BUSINESS_PERMIT));
        assertEquals(idFront, result.get(MerchantApplicationDocument.TYPE_ID_FRONT));
    }

    @Test
    void T17_findDocumentMap_deleteFlagTrueの書類のみのとき空のMapを返す() {
        when(documentRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Collections.emptyList());

        assertTrue(service.findDocumentMap(MEMBER_CODE).isEmpty());
    }

    @Test
    void T18_findDocumentMap_同一memberCodeかつ同一documentTypeが複数ある場合は最初のレコードが優先される() {
        MerchantApplicationDocument first = buildDocument(
                MerchantApplicationDocument.TYPE_BUSINESS_PERMIT, "first.pdf");
        MerchantApplicationDocument second = buildDocument(
                MerchantApplicationDocument.TYPE_BUSINESS_PERMIT, "second.pdf");
        when(documentRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(List.of(first, second));

        Map<String, MerchantApplicationDocument> result = service.findDocumentMap(MEMBER_CODE);

        assertEquals(1, result.size());
        assertEquals("first.pdf", result.get(MerchantApplicationDocument.TYPE_BUSINESS_PERMIT).getFileName());
    }

    @Test
    void T19_findDocumentMap_異なるdocumentTypeの書類が複数ある場合は全てMapに含まれる() {
        MerchantApplicationDocument permit = buildDocument(
                MerchantApplicationDocument.TYPE_BUSINESS_PERMIT, "permit.pdf");
        MerchantApplicationDocument storePhoto = buildDocument(
                MerchantApplicationDocument.TYPE_STORE_PHOTO, "store.jpg");
        when(documentRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(List.of(permit, storePhoto));

        Map<String, MerchantApplicationDocument> result = service.findDocumentMap(MEMBER_CODE);

        assertEquals(2, result.size());
        assertTrue(result.containsKey(MerchantApplicationDocument.TYPE_BUSINESS_PERMIT));
        assertTrue(result.containsKey(MerchantApplicationDocument.TYPE_STORE_PHOTO));
    }

    // =========================================================
    // updateApplication 入力検証（項番20〜54）
    // =========================================================

    @Test
    void T20_updateApplication_memberCodeがnullのとき会員コードが指定されていませんをスロー() {
        Map<String, String> data = buildValidData();
        data.remove("memberCode");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateApplication(data));

        assertEquals("会員コードが指定されていません", ex.getMessage());
    }

    @Test
    void T21_updateApplication_memberCodeが空文字のとき会員コードが指定されていませんをスロー() {
        Map<String, String> data = buildValidData();
        data.put("memberCode", "");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateApplication(data));

        assertEquals("会員コードが指定されていません", ex.getMessage());
    }

    @Test
    void T22_updateApplication_存在しないmemberCodeのとき申込情報が見つかりませんをスロー() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.empty());
        Map<String, String> data = buildValidData();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateApplication(data));

        assertTrue(ex.getMessage().contains("申込情報が見つかりません"));
    }

    @Test
    void T23_updateApplication_全必須項目が設定済みの正常データのとき例外なく保存が完了する() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();

        assertDoesNotThrow(() -> service.updateApplication(data));

        verify(applicationRepository).save(existing);
    }

    @Test
    void T24_updateApplication_agreedStarpayがtrueでないときバリデーションエラーが発生する() {
        Map<String, String> data = buildValidData();
        data.put("agreedStarpay", "false");

        assertValidationError(data, "StarPay決済サービス加盟店規約への確認が必要です");
    }

    @Test
    void T25_updateApplication_agreedJcbがtrueでないときバリデーションエラーが発生する() {
        Map<String, String> data = buildValidData();
        data.put("agreedJcb", "false");

        assertValidationError(data, "JCB加盟店規約・加盟店特約への確認が必要です");
    }

    @Test
    void T26_updateApplication_agreedRyuginVisaMcCuがtrueでないときバリデーションエラーが発生する() {
        Map<String, String> data = buildValidData();
        data.put("agreedRyuginVisaMcCu", "false");

        assertValidationError(data, "琉球銀行加盟店規約（Visa/Mastercard/銀聯）への確認が必要です");
    }

    @Test
    void T27_updateApplication_agreedRyuginCuQrがtrueでないときバリデーションエラーが発生する() {
        Map<String, String> data = buildValidData();
        data.put("agreedRyuginCuQr", "false");

        assertValidationError(data, "銀聯QRコード決済サービス利用加盟店規約への確認が必要です");
    }

    @Test
    void T28_updateApplication_agreedAgencyDelegationがtrueでないときバリデーションエラーが発生する() {
        Map<String, String> data = buildValidData();
        data.put("agreedAgencyDelegation", "false");

        assertValidationError(data, "代理申請の委任への同意が必要です");
    }

    @Test
    void T29_updateApplication_agreedServiceTermsがtrueでないときバリデーションエラーが発生する() {
        Map<String, String> data = buildValidData();
        data.put("agreedServiceTerms", "false");

        assertValidationError(data, "当社サービス利用規約への同意が必要です");
    }

    @Test
    void T30_updateApplication_agreedPrivacyPolicyがtrueでないときバリデーションエラーが発生する() {
        Map<String, String> data = buildValidData();
        data.put("agreedPrivacyPolicy", "false");

        assertValidationError(data, "当社プライバシーポリシーへの同意が必要です");
    }

    @Test
    void T31_updateApplication_agreedAuthorityConfirmedがtrueでないときバリデーションエラーが発生する() {
        Map<String, String> data = buildValidData();
        data.put("agreedAuthorityConfirmed", "false");

        assertValidationError(data, "代表者・契約締結権限の確認が必要です");
    }

    @Test
    void T32_updateApplication_取引形態が全てfalseのとき取引形態を1つ以上選択してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("txTypeVisitSales", "false");

        assertValidationError(data, "取引形態を1つ以上選択してください");
    }

    @Test
    void T33_updateApplication_businessEntityTypeが空のとき法人区分は必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("businessEntityType", "");

        assertValidationError(data, "法人区分は必須です");
    }

    @Test
    void T34_updateApplication_salesFormatが空のとき販売形態は必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("salesFormat", "");

        assertValidationError(data, "販売形態は必須です");
    }

    @Test
    void T35_updateApplication_operationFormatが空のとき運営形態は必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("operationFormat", "");

        assertValidationError(data, "運営形態は必須です");
    }

    @Test
    void T36_updateApplication_QRコード決済が全てfalseのときQRコード決済を1つ以上選択してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("payQrPaypay", "false");

        assertValidationError(data, "QRコード決済を1つ以上選択してください");
    }

    @Test
    void T37_updateApplication_クレジットカード決済が全てfalseのときクレジットカード決済を1つ以上選択してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("payCreditJcb", "false");

        assertValidationError(data, "クレジットカード決済を1つ以上選択してください");
    }

    @Test
    void T38_updateApplication_電子マネー決済が全てfalseのとき電子マネー決済を1つ以上選択してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("payEmoneyId", "false");

        assertValidationError(data, "電子マネー決済を1つ以上選択してください");
    }

    @Test
    void T39_updateApplication_corporateNameが空のとき法人名は必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("corporateName", "");

        assertValidationError(data, "法人名は必須です");
    }

    @Test
    void T40_updateApplication_corporateNumberが空のとき法人番号は必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("corporateNumber", "");

        assertValidationError(data, "法人番号は必須です");
    }

    @Test
    void T41_updateApplication_repLastNameが空のとき代表者姓は必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("repLastName", "");

        assertValidationError(data, "代表者 姓は必須です");
    }

    @Test
    void T42_updateApplication_contactEmailが空のとき担当者メールアドレスは必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("contactEmail", "");

        assertValidationError(data, "担当者 メールアドレスは必須です");
    }

    @Test
    void T43_updateApplication_bankCodeが空のとき金融機関コードは必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("bankCode", "");

        assertValidationError(data, "金融機関コードは必須です");
    }

    @Test
    void T44_updateApplication_accountHolderKanaが空のとき口座名義カナは必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("accountHolderKana", "");

        assertValidationError(data, "口座名義（カナ）は必須です");
    }

    @Test
    void T45_updateApplication_storeCountが0のとき店舗数は1以上を入力してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("storeCount", "0");

        assertValidationError(data, "店舗数は1以上を入力してください");
    }

    @Test
    void T46_updateApplication_storeCountが数値以外のとき店舗数に正しい数値を入力してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("storeCount", "abc");

        assertValidationError(data, "店舗数に正しい数値を入力してください");
    }

    @Test
    void T47_updateApplication_storeCountが空のとき店舗数は必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("storeCount", "");

        assertValidationError(data, "店舗数は必須です");
    }

    @Test
    void T48_updateApplication_storeAveragePriceがマイナス1のとき平均単価は0以上を入力してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("storeAveragePrice", "-1");

        assertValidationError(data, "平均単価は0以上を入力してください");
    }

    @Test
    void T49_updateApplication_storeAveragePriceが0のときエラーが含まれない() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.put("storeAveragePrice", "0");

        assertDoesNotThrow(() -> service.updateApplication(data));

        assertEquals(0, existing.getStoreAveragePrice());
    }

    @Test
    void T50_updateApplication_storeAveragePriceが数値以外のとき平均単価に正しい数値を入力してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("storeAveragePrice", "abc");

        assertValidationError(data, "平均単価に正しい数値を入力してください");
    }

    @Test
    void T51_updateApplication_mposQuantityが0のとき端末台数は1以上を入力してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("mposQuantity", "0");

        assertValidationError(data, "端末台数は1以上を入力してください");
    }

    @Test
    void T52_updateApplication_mposQuantityが数値以外のとき端末台数に正しい数値を入力してくださいエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("mposQuantity", "abc");

        assertValidationError(data, "端末台数に正しい数値を入力してください");
    }

    @Test
    void T53_updateApplication_deliveryZipCodeが空のときお届け先郵便番号は必須ですエラーが含まれる() {
        Map<String, String> data = buildValidData();
        data.put("deliveryZipCode", "");

        assertValidationError(data, "お届け先 郵便番号は必須です");
    }

    @Test
    void T54_updateApplication_複数のバリデーションエラーがある場合は全エラーが改行区切りで例外メッセージに含まれる() {
        Map<String, String> data = buildValidData();
        data.put("corporateName", "");
        data.put("bankCode", "");
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(new MerchantApplication()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateApplication(data));

        assertTrue(ex.getMessage().contains("法人名は必須です"));
        assertTrue(ex.getMessage().contains("金融機関コードは必須です"));
        assertTrue(ex.getMessage().contains("\n"));
    }

    // =========================================================
    // updateApplication 型変換（項番55〜69）
    // =========================================================

    @Test
    void T55_updateApplication_sにnullを渡すとエンティティのフィールドにnullがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.remove("companyUrl");

        service.updateApplication(data);

        assertNull(existing.getCompanyUrl());
    }

    @Test
    void T56_updateApplication_sに空文字を渡すとエンティティのフィールドにnullがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.put("companyUrl", "");

        service.updateApplication(data);

        assertNull(existing.getCompanyUrl());
    }

    @Test
    void T57_updateApplication_sに有効な文字列を渡すとその文字列がセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.put("companyUrl", "https://example.co.jp");

        service.updateApplication(data);

        assertEquals("https://example.co.jp", existing.getCompanyUrl());
    }

    @Test
    void T58_updateApplication_bにtrueを渡すとtrueがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.put("closedMonday", "true");

        service.updateApplication(data);

        assertTrue(existing.isClosedMonday());
    }

    @Test
    void T59_updateApplication_bにfalseを渡すとfalseがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.put("closedMonday", "false");

        service.updateApplication(data);

        assertFalse(existing.isClosedMonday());
    }

    @Test
    void T60_updateApplication_bにnullを渡すとfalseがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.remove("closedMonday");

        service.updateApplication(data);

        assertFalse(existing.isClosedMonday());
    }

    @Test
    void T61_updateApplication_dに2025年01月01日を渡すと正しいLocalDateがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.put("mapDisplayDesiredDate", "2025-01-01");

        service.updateApplication(data);

        assertEquals(LocalDate.of(2025, 1, 1), existing.getMapDisplayDesiredDate());
    }

    @Test
    void T62_updateApplication_dに不正な日付文字列を渡すとIllegalArgumentExceptionをスロー() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(new MerchantApplication()));
        Map<String, String> data = buildValidData();
        data.put("mapDisplayDesiredDate", "2025-99-99");

        assertThrows(IllegalArgumentException.class, () -> service.updateApplication(data));
    }

    @Test
    void T63_updateApplication_dにnullを渡すとnullがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.remove("mapDisplayDesiredDate");

        service.updateApplication(data);

        assertNull(existing.getMapDisplayDesiredDate());
    }

    @Test
    void T64_updateApplication_tに0900を渡すと正しいLocalTimeがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.put("businessHours1Start", "09:00");

        service.updateApplication(data);

        assertEquals(LocalTime.of(9, 0), existing.getBusinessHours1Start());
    }

    @Test
    void T65_updateApplication_tに不正な時刻文字列を渡すとIllegalArgumentExceptionをスロー() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(new MerchantApplication()));
        Map<String, String> data = buildValidData();
        data.put("businessHours1Start", "25:99");

        assertThrows(IllegalArgumentException.class, () -> service.updateApplication(data));
    }

    @Test
    void T66_updateApplication_iに123を渡すと123がセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.put("employeeCount", "123");

        service.updateApplication(data);

        assertEquals(123, existing.getEmployeeCount());
    }

    @Test
    void T67_updateApplication_iにnullを渡すとnullがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.remove("employeeCount");

        service.updateApplication(data);

        assertNull(existing.getEmployeeCount());
    }

    @Test
    void T68_updateApplication_lに1000000を渡すと1000000がセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.put("capitalAmount", "1000000");

        service.updateApplication(data);

        assertEquals(1000000L, existing.getCapitalAmount());
    }

    @Test
    void T69_updateApplication_lにnullを渡すとnullがセットされる() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));
        Map<String, String> data = buildValidData();
        data.remove("capitalAmount");

        service.updateApplication(data);

        assertNull(existing.getCapitalAmount());
    }

    // =========================================================
    // deleteApplication（項番70〜75）
    // =========================================================

    @Test
    void T70_deleteApplication_memberCodeがnullのとき会員コードが指定されていませんをスロー() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.deleteApplication(null));

        assertEquals("会員コードが指定されていません", ex.getMessage());
    }

    @Test
    void T71_deleteApplication_memberCodeが空文字のとき会員コードが指定されていませんをスロー() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.deleteApplication(""));

        assertEquals("会員コードが指定されていません", ex.getMessage());
    }

    @Test
    void T72_deleteApplication_存在しないmemberCodeのとき申込情報が見つかりませんをスロー() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.deleteApplication(MEMBER_CODE));

        assertTrue(ex.getMessage().contains("申込情報が見つかりません"));
    }

    @Test
    void T73_deleteApplication_deleteFlagTrueのレコードのmemberCodeのときIllegalArgumentExceptionをスロー() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.deleteApplication(MEMBER_CODE));
    }

    @Test
    void T74_deleteApplication_正常なmemberCodeのときdeleteFlagがtrueに設定される() {
        MerchantApplication existing = new MerchantApplication();
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(existing));

        service.deleteApplication(MEMBER_CODE);

        assertTrue(existing.isDeleteFlag());
    }

    @Test
    void T75_deleteApplication_正常なmemberCodeのときlogicalDeleteByMemberCodeが呼ばれ書類も論理削除される() {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(new MerchantApplication()));

        service.deleteApplication(MEMBER_CODE);

        verify(documentRepository).logicalDeleteByMemberCode(MEMBER_CODE);
    }

    // =========================================================
    // テストデータ生成ヘルパー
    // =========================================================

    private void assertValidationError(Map<String, String> data, String expectedMessage) {
        when(applicationRepository.findByMemberCodeAndDeleteFlagFalse(MEMBER_CODE))
                .thenReturn(Optional.of(new MerchantApplication()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateApplication(data));

        assertTrue(ex.getMessage().contains(expectedMessage),
                "期待するエラーメッセージが含まれていません: " + ex.getMessage());
    }

    private MerchantApplicationDocument buildDocument(String documentType, String fileName) {
        MerchantApplicationDocument doc = new MerchantApplicationDocument();
        doc.setMemberCode(MEMBER_CODE);
        doc.setDocumentType(documentType);
        doc.setFileName(fileName);
        return doc;
    }

    /**
     * validateUpdateData のすべての必須項目・選択グループを満たす正常データを生成する。
     * 各テストは、このデータの一部を上書き／削除して異常系を再現する。
     */
    private Map<String, String> buildValidData() {
        Map<String, String> data = new HashMap<>();
        data.put("memberCode", MEMBER_CODE);
        data.put("applicationStatus", MerchantApplication.STATUS_UNREVIEWED);

        // STEP1: 事前確認
        data.put("agreedStarpay", "true");
        data.put("agreedJcb", "true");
        data.put("agreedRyuginVisaMcCu", "true");
        data.put("agreedRyuginCuQr", "true");
        data.put("agreedAgencyDelegation", "true");
        data.put("agreedServiceTerms", "true");
        data.put("agreedPrivacyPolicy", "true");
        data.put("agreedAuthorityConfirmed", "true");

        // STEP2: 取引形態
        data.put("txTypeVisitSales", "true");
        data.put("txTypeContinuousService", "false");
        data.put("txTypePhoneSolicitation", "false");
        data.put("txTypePrepaidService", "false");
        data.put("txTypeBusinessInduction", "false");
        data.put("txTypeChainSales", "false");
        data.put("txTypeNoneApplicable", "false");
        data.put("businessEntityType", "法人");
        data.put("salesFormat", "店舗販売");
        data.put("operationFormat", "自社運営");

        // STEP3: QRコード決済
        data.put("payQrPaypay", "true");
        data.put("payQrDBarai", "false");
        data.put("payQrRakutenPay", "false");
        data.put("payQrAlipayPlus", "false");
        data.put("payQrWechatPay", "false");
        data.put("payQrAuPay", "false");
        data.put("payQrMerpay", "false");
        data.put("payQrJkoPay", "false");

        // STEP3: クレジットカード決済
        data.put("payCreditJcb", "true");
        data.put("payCreditVisa", "false");
        data.put("payCreditMastercard", "false");
        data.put("payCreditDiscover", "false");
        data.put("payCreditDiners", "false");
        data.put("payCreditAmex", "false");
        data.put("payCreditBonus", "false");
        data.put("payCreditTwoTimes", "false");
        data.put("payCreditInstallment", "false");
        data.put("payCreditRevolving", "false");

        // STEP3: 電子マネー決済
        data.put("payEmoneyId", "true");
        data.put("payEmoneyWaon", "false");
        data.put("payEmoneyRakutenEdy", "false");
        data.put("payEmoneyNanaco", "false");
        data.put("payEmoneyTransitIc", "false");
        data.put("payEmoneyQuickPay", "false");
        data.put("payEmoneyApplePay", "false");

        // STEP4: 法人情報
        data.put("corporateNumber", "1234567890123");
        data.put("corporateName", "テスト株式会社");
        data.put("corporateNameKana", "テストカブシキガイシャ");
        data.put("corporateNameEn", "Test Inc.");
        data.put("establishmentDate", "2000-04-01");
        data.put("corporateType", "株式会社");
        data.put("brandName", "テストブランド");
        data.put("brandNameKana", "テストブランド");
        data.put("brandNameEn", "Test Brand");
        data.put("companyUrl", "https://example.co.jp");
        data.put("annualRevenue", "100000000");
        data.put("capitalAmount", "10000000");
        data.put("employeeCount", "10");
        data.put("industryCategory", "小売業");
        data.put("industryDetail", "衣料品店");
        data.put("businessDescription", "衣料品の販売");

        // 本社住所
        data.put("companyZipCode", "1000001");
        data.put("companyPrefecture", "東京都");
        data.put("companyPrefectureKana", "トウキョウト");
        data.put("companyCity", "千代田区");
        data.put("companyCityKana", "チヨダク");
        data.put("companyTown", "千代田");
        data.put("companyTownKana", "チヨダ");
        data.put("companyStreetNumber", "1-1");
        data.put("companyStreetNumberKana", "1-1");
        data.put("companyBuilding", "テストビル");
        data.put("companyBuildingKana", "テストビル");
        data.put("companyPhone", "0312345678");
        data.put("companyFax", "0312345679");
        data.put("companyMobile", "09012345678");

        // 代表者
        data.put("repLastName", "山田");
        data.put("repLastNameKana", "ヤマダ");
        data.put("repLastNameEn", "Yamada");
        data.put("repFirstName", "太郎");
        data.put("repFirstNameKana", "タロウ");
        data.put("repFirstNameEn", "Taro");
        data.put("repBirthDate", "1980-01-01");
        data.put("repGender", "male");
        data.put("repZipCode", "1000001");
        data.put("repPrefecture", "東京都");
        data.put("repPrefectureKana", "トウキョウト");
        data.put("repCity", "千代田区");
        data.put("repCityKana", "チヨダク");
        data.put("repTown", "千代田");
        data.put("repTownKana", "チヨダ");
        data.put("repStreetNumber", "1-1");
        data.put("repStreetNumberKana", "1-1");
        data.put("repBuilding", "テストビル");
        data.put("repPhone", "0312345678");

        // 担当者
        data.put("contactLastName", "鈴木");
        data.put("contactLastNameKana", "スズキ");
        data.put("contactFirstName", "花子");
        data.put("contactFirstNameKana", "ハナコ");
        data.put("contactZipCode", "1000001");
        data.put("contactPrefecture", "東京都");
        data.put("contactPrefectureKana", "トウキョウト");
        data.put("contactCity", "千代田区");
        data.put("contactCityKana", "チヨダク");
        data.put("contactTown", "千代田");
        data.put("contactTownKana", "チヨダ");
        data.put("contactStreetNumber", "1-1");
        data.put("contactStreetNumberKana", "1-1");
        data.put("contactBuilding", "テストビル");
        data.put("contactBuildingKana", "テストビル");
        data.put("contactDepartment", "総務部");
        data.put("contactEmail", "contact@example.co.jp");
        data.put("contactPhone1", "0312345679");
        data.put("contactPhone2", "0312345680");

        // STEP5: 口座
        data.put("bankCode", "0001");
        data.put("bankName", "テスト銀行");
        data.put("branchCode", "001");
        data.put("branchName", "本店");
        data.put("accountType", "普通");
        data.put("accountNumber", "1234567");
        data.put("accountHolderKana", "テストカブシキガイシャ");

        // STEP6: 店舗
        data.put("storeName", "テスト店");
        data.put("storeNameKana", "テストテン");
        data.put("storeNameEn", "Test Store");
        data.put("storeBrandName", "テストブランド");
        data.put("storeBrandNameKana", "テストブランド");
        data.put("storeBrandNameEn", "Test Brand");
        data.put("storeIndustryCategory", "小売業");
        data.put("storeIndustryDetail", "衣料品店");
        data.put("storeProductDescription", "衣料品");
        data.put("storeCount", "1");
        data.put("storeAveragePrice", "3000");
        data.put("storeBankAccount", "本店口座");
        data.put("storeReceiptName", "テスト店");
        data.put("mapDisplayDesired", "false");
        data.put("businessHours1Start", "09:00");
        data.put("businessHours1End", "18:00");
        data.put("closedMonday", "false");
        data.put("closedTuesday", "false");
        data.put("closedWednesday", "false");
        data.put("closedThursday", "false");
        data.put("closedFriday", "false");
        data.put("closedSaturday", "false");
        data.put("closedSunday", "false");
        data.put("closedHoliday", "false");
        data.put("closedHolidayEve", "false");

        // 店舗住所
        data.put("shopZipCode", "1000001");
        data.put("shopPrefecture", "東京都");
        data.put("shopPrefectureKana", "トウキョウト");
        data.put("shopCity", "千代田区");
        data.put("shopCityKana", "チヨダク");
        data.put("shopTown", "千代田");
        data.put("shopTownKana", "チヨダ");
        data.put("shopStreetNumber", "1-1");
        data.put("shopStreetNumberKana", "1-1");
        data.put("shopBuilding", "テストビル");
        data.put("shopBuildingKana", "テストビル");
        data.put("shopPhone", "0312345680");
        data.put("terminalPossessionStatus", "未所持");
        data.put("terminalIcStatus", "対応");

        // STEP8: 発送
        data.put("mposQuantity", "1");
        data.put("deliveryZipCode", "1000001");
        data.put("deliveryPrefecture", "東京都");
        data.put("deliveryPrefectureKana", "トウキョウト");
        data.put("deliveryCity", "千代田区");
        data.put("deliveryCityKana", "チヨダク");
        data.put("deliveryTown", "千代田");
        data.put("deliveryTownKana", "チヨダ");
        data.put("deliveryStreetNumber", "1-1");
        data.put("deliveryStreetNumberKana", "1-1");
        data.put("deliveryBuilding", "テストビル");
        data.put("deliveryBuildingKana", "テストビル");
        data.put("deliveryPhone", "0312345681");
        data.put("deliveryReceiver", "山田太郎");

        return data;
    }
}
