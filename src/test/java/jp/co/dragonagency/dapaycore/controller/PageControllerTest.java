package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.FeeRateListItemDto;
import jp.co.dragonagency.dapaycore.dto.MemberListItemDto;
import jp.co.dragonagency.dapaycore.service.FeeRateService;
import jp.co.dragonagency.dapaycore.service.MemberListService;
import jp.co.dragonagency.dapaycore.service.MerchantApplicationInquiryService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @InjectMocks
    private PageController controller;

    @Test
    void showMemberList_membersAttributeIsFindAllResult() {
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
    void showMemberList_returnsViewNameMemberList() {
        when(memberListService.findAll()).thenReturn(Collections.emptyList());
        Model model = new ExtendedModelMap();

        String viewName = controller.showMemberList(model);

        assertEquals("member_list", viewName);
    }

    @Test
    void showMemberList_membersAttributeIsEmptyListWhenNoData() {
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
