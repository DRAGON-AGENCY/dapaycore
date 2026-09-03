package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.FeeRateListItemDto;
import jp.co.dragonagency.dapaycore.service.FeeRateService;
import jp.co.dragonagency.dapaycore.service.MemberListService;
import jp.co.dragonagency.dapaycore.service.MerchantApplicationInquiryService;
import jp.co.dragonagency.dapaycore.service.NetStarsSettlementImportService;
import jp.co.dragonagency.dapaycore.service.TransferFeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * operation_fee_list.html のテンプレート表示を検証するテスト。
 * 単体テスト仕様書_手数料一覧_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T8〜T14（■ テストケース一覧）に対応する。
 * INPUT データ:
 * C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_手数料一覧_v1.00.xlsx
 */
@WebMvcTest(PageController.class)
class FeeRateListTemplateRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberListService memberListService;

    @MockBean
    private MerchantApplicationInquiryService inquiryService;

    @MockBean
    private FeeRateService feeRateService;

    @MockBean
    private TransferFeeService transferFeeService;

    @MockBean
    private NetStarsSettlementImportService netStarsSettlementImportService;

    @Test
    void T8_一覧_feeRatesの件数分のdataRowがレンダリングされる() throws Exception {
        when(feeRateService.findAllForList()).thenReturn(List.of(
                feeRate("FEE-A01", "カブシキガイシャアルファ", "2026-04-01",
                        null, "3.25", "valid"),
                feeRate("FEE-A02", "", "2026-04-10",
                        "2026-12-31", "3.20", "valid"),
                feeRate("FEE-A03", "カナサン", "2026-05-01",
                        "2026-04-01", "3.50", "expired")));

        String html = render();

        assertEquals(3, countOccurrences(html, "class=\"data-row\""));
    }

    @Test
    void T9_一覧_statusに応じた状態ラベルとバッジクラスが描画される()
            throws Exception {
        when(feeRateService.findAllForList()).thenReturn(List.of(
                feeRate("FEE-V", "カナ", "2026-04-01", null, "3.00", "valid"),
                feeRate("FEE-F", "カナ", "2027-04-01", null, "3.00", "future"),
                feeRate("FEE-E", "カナ", "2025-01-01", "2025-12-31",
                        "3.00", "expired")));

        String html = render();

        assertThat(html, matchesPattern(
                "(?s).*<span class=\"badge badge-valid\"\\s*>有効</span>.*"));
        assertThat(html, matchesPattern(
                "(?s).*<span class=\"badge badge-future\"\\s*>予定</span>.*"));
        assertThat(html, matchesPattern(
                "(?s).*<span class=\"badge badge-expired\"\\s*>終了</span>.*"));
    }

    @Test
    void T10_一覧_適用終了日がnullの行はダッシュ表示() throws Exception {
        when(feeRateService.findAllForList()).thenReturn(List.of(
                feeRate("FEE-A01", "カナ", "2026-04-01", null, "3.25", "valid"),
                feeRate("FEE-A02", "カナ", "2026-04-01", "2026-12-31",
                        "3.20", "valid")));

        String html = render();

        assertThat(html, containsString("<td class=\"mono\">\u2014</td>"));
        assertThat(html, containsString("<td class=\"mono\">2026-12-31</td>"));
    }

    @Test
    void T11_一覧_手数料率はパーセント付きで表示される() throws Exception {
        when(feeRateService.findAllForList()).thenReturn(List.of(
                feeRate("FEE-A01", "カナ", "2026-04-01", null, "3.25", "valid")));

        String html = render();

        assertThat(html, containsString("<td class=\"rate\">3.25%</td>"));
    }

    @Test
    void T12_一覧_各行にdata属性が埋め込まれる() throws Exception {
        when(feeRateService.findAllForList()).thenReturn(List.of(
                feeRate(9001L, "FEE-A01", "カブシキガイシャアルファ",
                        "2026-04-01", null, "3.25", "valid")));

        String html = render();

        assertThat(html, containsString("data-id=\"9001\""));
        assertThat(html, containsString("data-member=\"FEE-A01\""));
        assertThat(html, containsString("data-kana=\"カブシキガイシャアルファ\""));
        assertThat(html, containsString("data-status=\"valid\""));
    }

    @Test
    void T13_一覧_0件でも空状態行がレンダリングされる() throws Exception {
        when(feeRateService.findAllForList())
                .thenReturn(Collections.emptyList());

        String html = render();

        assertThat(html, containsString("id=\"empty-row\""));
        assertThat(html, containsString("該当するデータがありません。"));
    }

    @Test
    void T14_未認証のとき運用管理ログイン画面へリダイレクトする() throws Exception {
        mockMvc.perform(get("/operation_fee_list.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_operation.html"));
    }

    // =========================================================
    // ヘルパー
    // =========================================================

    private String render() throws Exception {
        MvcResult result = mockMvc.perform(get("/operation_fee_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    private static FeeRateListItemDto feeRate(
            String memberCode, String corporateNameKana, String startDate,
            String endDate, String feeRateDisplay, String status) {
        return feeRate(1L, memberCode, corporateNameKana, startDate,
                endDate, feeRateDisplay, status);
    }

    private static FeeRateListItemDto feeRate(
            long id, String memberCode, String corporateNameKana,
            String startDate, String endDate, String feeRateDisplay,
            String status) {
        FeeRateListItemDto dto = new FeeRateListItemDto();
        dto.setId(id);
        dto.setMemberCode(memberCode);
        dto.setCorporateNameKana(corporateNameKana);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setFeeRateDisplay(feeRateDisplay);
        dto.setStatus(status);
        return dto;
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        int index = text.indexOf(token);
        while (index >= 0) {
            count++;
            index = text.indexOf(token, index + token.length());
        }
        return count;
    }
}
