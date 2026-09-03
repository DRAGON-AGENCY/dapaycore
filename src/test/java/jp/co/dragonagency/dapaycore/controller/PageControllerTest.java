package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.FeeRateListItemDto;
import jp.co.dragonagency.dapaycore.dto.MemberListItemDto;
import jp.co.dragonagency.dapaycore.dto.TransferFeeListItemDto;
import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
import jp.co.dragonagency.dapaycore.dto.NetStarsImportControlView;
import jp.co.dragonagency.dapaycore.dto.NetStarsImportHistoryListItemDto;
import jp.co.dragonagency.dapaycore.service.FeeRateService;
import jp.co.dragonagency.dapaycore.service.MemberListService;
import jp.co.dragonagency.dapaycore.service.MerchantApplicationInquiryService;
import jp.co.dragonagency.dapaycore.service.NetStarsSettlementImportService;
import jp.co.dragonagency.dapaycore.service.TransferFeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    @Mock
    private MemberListService memberListService;

    @Mock
    private MerchantApplicationInquiryService inquiryService;

    @Mock
    private FeeRateService feeRateService;

    @Mock
    private TransferFeeService transferFeeService;

    @Mock
    private NetStarsSettlementImportService netStarsSettlementImportService;

    @InjectMocks
    private PageController controller;

    // =========================================================
    // 単体テスト仕様書_会員一覧_v1.00 項番 T14〜T16（PageController#showMemberList）
    // INPUT: C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_会員一覧_v1.00.xlsx
    // =========================================================

    @Test
    void T14_showMemberList_membersにサービスの戻り値が設定される() {
        List<MemberListItemDto> expected = List.of(
                new MemberListItemDto(
                        "M001", "テスト株式会社", "山田", "太郎",
                        "UNREVIEWED", LocalDateTime.of(2026, 6, 1, 10, 0), 2L));
        when(memberListService.findAll()).thenReturn(expected);
        Model model = new ExtendedModelMap();

        controller.showMemberList(model);

        assertEquals(expected, model.getAttribute("members"));
        verify(memberListService).findAll();
    }

    @Test
    void T16_showMemberList_ビュー名member_listを返す() {
        when(memberListService.findAll()).thenReturn(Collections.emptyList());
        Model model = new ExtendedModelMap();

        String viewName = controller.showMemberList(model);

        assertEquals("member_list", viewName);
    }

    @Test
    void T15_showMemberList_データなしのときmembersが空リスト() {
        when(memberListService.findAll()).thenReturn(Collections.emptyList());
        Model model = new ExtendedModelMap();

        controller.showMemberList(model);

        assertEquals(Collections.emptyList(), model.getAttribute("members"));
    }

    // ===== 項番1: showOperationFeeList - feeRates がモデルに設定される =====

    @Test
    void T01_showOperationFeeList_feeRatesAttributeIsFindAllForListResult() {
        FeeRateListItemDto item = new FeeRateListItemDto();
        item.setMemberCode("FE001");
        item.setCorporateNameKana("フィーテスト カブシキガイシャ");
        item.setStartDate("2026-01-01");
        item.setEndDate("2026-12-31");
        item.setFeeRateDisplay("3.24");
        item.setStatus("valid");
        List<FeeRateListItemDto> expected = List.of(item);
        when(feeRateService.findAllForList()).thenReturn(expected);
        Model model = new ExtendedModelMap();

        controller.showOperationFeeList(model);

        assertEquals(expected, model.getAttribute("feeRates"));
        verify(feeRateService).findAllForList();
    }

    // ===== 項番2: showOperationFeeList - delete_flag=false のレコードが含まれる =====

    @Test
    void T01b_showOperationFeeList_feeRatesAttributeContainsActiveRecords() {
        FeeRateListItemDto active = new FeeRateListItemDto();
        active.setMemberCode("FE001");
        active.setStatus("valid");
        when(feeRateService.findAllForList()).thenReturn(List.of(active));
        Model model = new ExtendedModelMap();

        controller.showOperationFeeList(model);

        List<?> feeRates = (List<?>) model.getAttribute("feeRates");
        assertEquals(1, feeRates.size());
    }

    // ===== 項番3: showOperationFeeList - データなしのとき feeRates が空リスト =====

    @Test
    void T01c_showOperationFeeList_feeRatesAttributeIsEmptyListWhenNoData() {
        when(feeRateService.findAllForList()).thenReturn(Collections.emptyList());
        Model model = new ExtendedModelMap();

        controller.showOperationFeeList(model);

        assertEquals(Collections.emptyList(), model.getAttribute("feeRates"));
    }

    // ===== 項番4: showOperationFeeEdit - ビュー名 "operation_fee_edit" を返す =====

    @Test
    void T02_showOperationFeeEdit_returnsViewNameOperationFeeEdit() {
        String viewName = controller.showOperationFeeEdit();

        assertEquals("operation_fee_edit", viewName);
    }

    // ===== 項番01: showOperationTransferFeeList - transferFees がモデルに設定される =====

    @Test
    void T01_showOperationTransferFeeList_transferFeesAttributeIsFindAllForListResult() {
        TransferFeeListItemDto item = new TransferFeeListItemDto();
        item.setBankCode("0310");
        item.setTransferFee(200);
        List<TransferFeeListItemDto> expected = List.of(item);
        when(transferFeeService.findAllForList()).thenReturn(expected);
        Model model = new ExtendedModelMap();

        controller.showOperationTransferFeeList(model);

        assertEquals(expected, model.getAttribute("transferFees"));
        verify(transferFeeService).findAllForList();
    }

    // ===== 項番02: showOperationTransferFeeList - データなしのとき transferFees が空リスト =====

    @Test
    void T02_showOperationTransferFeeList_transferFeesAttributeIsEmptyListWhenNoData() {
        when(transferFeeService.findAllForList()).thenReturn(Collections.emptyList());
        Model model = new ExtendedModelMap();

        controller.showOperationTransferFeeList(model);

        assertEquals(Collections.emptyList(), model.getAttribute("transferFees"));
    }

    // ===== 項番03: showOperationTransferFeeList - ビュー名 "operation_transfer_fee_list" を返す =====

    @Test
    void T03_showOperationTransferFeeList_returnsViewNameOperationTransferFeeList() {
        when(transferFeeService.findAllForList()).thenReturn(Collections.emptyList());
        Model model = new ExtendedModelMap();

        String viewName = controller.showOperationTransferFeeList(model);

        assertEquals("operation_transfer_fee_list", viewName);
    }

    // ===== 項番04: showOperationTransferFeeEdit - ビュー名 "operation_transfer_fee_edit" を返す =====

    @Test
    void T04_showOperationTransferFeeEdit_returnsViewNameOperationTransferFeeEdit() {
        String viewName = controller.showOperationTransferFeeEdit();

        assertEquals("operation_transfer_fee_edit", viewName);
    }

    // ===== showOperationNetStarsImport（単体テスト仕様書_還元データ取込履歴照会 T46〜T48） =====

    @Test
    void T46_showOperationNetStarsImport_importHistoriesとimportControlがモデルに設定される() {
        NetStarsImportHistoryListItemDto item = new NetStarsImportHistoryListItemDto();
        item.setId(1L);
        List<NetStarsImportHistoryListItemDto> expected = List.of(item);
        NetStarsImportControlView control =
                new NetStarsImportControlView(true, true, false, "", "");
        when(netStarsSettlementImportService.findHistoryForList()).thenReturn(expected);
        when(netStarsSettlementImportService.getControlView()).thenReturn(control);
        Model model = new ExtendedModelMap();

        controller.showOperationNetStarsImport(model);

        assertEquals(expected, model.getAttribute("importHistories"));
        assertEquals(control, model.getAttribute("importControl"));
        verify(netStarsSettlementImportService).findHistoryForList();
    }

    @Test
    void T47_showOperationNetStarsImport_データなしのときimportHistoriesが空リスト() {
        when(netStarsSettlementImportService.findHistoryForList())
                .thenReturn(Collections.emptyList());
        when(netStarsSettlementImportService.getControlView())
                .thenReturn(new NetStarsImportControlView(false, false, false,
                        "2026/09/01 10:00", "2026/09/06"));
        Model model = new ExtendedModelMap();

        controller.showOperationNetStarsImport(model);

        assertEquals(Collections.emptyList(), model.getAttribute("importHistories"));
        assertFalse(((NetStarsImportControlView)
                model.getAttribute("importControl")).enabled());
    }

    @Test
    void T48_showOperationNetStarsImport_ビュー名operation_netstars_importを返す() {
        when(netStarsSettlementImportService.findHistoryForList())
                .thenReturn(Collections.emptyList());
        when(netStarsSettlementImportService.getControlView())
                .thenReturn(new NetStarsImportControlView(true, false, false, "", ""));
        Model model = new ExtendedModelMap();

        String viewName = controller.showOperationNetStarsImport(model);

        assertEquals("operation_netstars_import", viewName);
    }

    // ===== 項番1〜7: showMerchantApplicationInquiry =====

    @Test
    void T01_showMerchantApplicationInquiry_transactionCodeがnullのときmerchantAppがnullでモデルに設定される() {
        Model model = new ExtendedModelMap();

        controller.showMerchantApplicationInquiry(null, model);

        assertNull(model.getAttribute("merchantApp"));
    }

    @Test
    void T02_showMerchantApplicationInquiry_transactionCodeがnullのときdocMapがモデルに追加されない() {
        Model model = new ExtendedModelMap();

        controller.showMerchantApplicationInquiry(null, model);

        assertNull(model.getAttribute("docMap"));
    }

    @Test
    void T03_showMerchantApplicationInquiry_transactionCodeが空文字のときmerchantAppがnullでモデルに設定される() {
        when(inquiryService.findApplication("")).thenReturn(null);
        Model model = new ExtendedModelMap();

        controller.showMerchantApplicationInquiry("", model);

        assertNull(model.getAttribute("merchantApp"));
    }

    @Test
    void T04_showMerchantApplicationInquiry_deleteFlagFalseで存在するtransactionCodeのときmerchantAppがモデルに設定される() {
        MerchantApplication expected = new MerchantApplication();
        expected.setMemberCode("TEST0001");
        when(inquiryService.findApplication("TEST0001")).thenReturn(expected);
        Model model = new ExtendedModelMap();

        controller.showMerchantApplicationInquiry("TEST0001", model);

        assertEquals(expected, model.getAttribute("merchantApp"));
    }

    @Test
    void T05_showMerchantApplicationInquiry_deleteFlagFalseで存在するtransactionCodeのときdocMapがモデルに設定される() {
        MerchantApplication app = new MerchantApplication();
        app.setMemberCode("TEST0001");
        Map<String, MerchantApplicationDocument> expected =
                Map.of(MerchantApplicationDocument.TYPE_BUSINESS_PERMIT, new MerchantApplicationDocument());
        when(inquiryService.findApplication("TEST0001")).thenReturn(app);
        when(inquiryService.findDocumentMap("TEST0001")).thenReturn(expected);
        Model model = new ExtendedModelMap();

        controller.showMerchantApplicationInquiry("TEST0001", model);

        assertEquals(expected, model.getAttribute("docMap"));
    }

    @Test
    void T06_showMerchantApplicationInquiry_deleteFlagTrueのtransactionCodeのときmerchantAppがnullでモデルに設定される() {
        when(inquiryService.findApplication("DELETED0001")).thenReturn(null);
        Model model = new ExtendedModelMap();

        controller.showMerchantApplicationInquiry("DELETED0001", model);

        assertNull(model.getAttribute("merchantApp"));
    }

    @Test
    void T07_showMerchantApplicationInquiry_存在しないtransactionCodeのときmerchantAppがnullでモデルに設定される() {
        when(inquiryService.findApplication("NOTFOUND")).thenReturn(null);
        Model model = new ExtendedModelMap();

        controller.showMerchantApplicationInquiry("NOTFOUND", model);

        assertNull(model.getAttribute("merchantApp"));
    }

    @Test
    void showMemberList_membersAttributeContainsAllReturnedItems() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 22, 9, 0);
        List<MemberListItemDto> expected = List.of(
                new MemberListItemDto("T001", "会社A", "鈴木", "一郎", "UNREVIEWED", now, 0L),
                new MemberListItemDto("T002", "会社B", "佐藤", "次郎", "REVIEWING", now, 1L),
                new MemberListItemDto("T003", "会社C", "田中", "三郎", "APPROVED", now, 3L));
        when(memberListService.findAll()).thenReturn(expected);
        Model model = new ExtendedModelMap();

        controller.showMemberList(model);

        assertEquals(3, ((List<?>) model.getAttribute("members")).size());
        assertEquals(expected, model.getAttribute("members"));
    }
}
