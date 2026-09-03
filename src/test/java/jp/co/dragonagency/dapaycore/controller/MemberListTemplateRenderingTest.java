package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.MemberListItemDto;
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

import java.time.LocalDateTime;
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
 * member_list.html のテンプレート表示を検証するテスト。
 * 単体テスト仕様書_会員一覧_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T17〜T22（■ テストケース一覧）に対応する。
 * INPUT データ:
 * C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_会員一覧_v1.00.xlsx
 */
@WebMvcTest(PageController.class)
class MemberListTemplateRenderingTest {

    private static final LocalDateTime SUBMITTED_AT =
            LocalDateTime.of(2026, 8, 25, 10, 0);

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
    void T17_一覧_membersの件数分のdataRowがレンダリングされる() throws Exception {
        when(memberListService.findAll()).thenReturn(List.of(
                member("MEM-L001", "株式会社アルファ商事", "山田", "太郎",
                        "APPROVED", SUBMITTED_AT, 3L),
                member("MEM-L002", null, "佐藤", "花子",
                        "REVIEWING", SUBMITTED_AT, 1L),
                member("MEM-L003", "有限会社ベータ", "鈴木", null,
                        "UNREVIEWED", SUBMITTED_AT, 0L),
                member("MEM-L004", "ガンマ合同会社", null, "次郎",
                        "REJECTED", null, 2L),
                member("MEM-L005", "デルタ株式会社", "田中", "三郎",
                        "APPROVED", SUBMITTED_AT, 5L)));

        String html = render();

        assertEquals(5, countOccurrences(html, "class=\"data-row\""));
    }

    @Test
    void T18_一覧_ステータスバッジがstatusClassとstatusLabelで描画される()
            throws Exception {
        when(memberListService.findAll()).thenReturn(List.of(
                member("MEM-L001", "会社A", "姓", "名",
                        "APPROVED", SUBMITTED_AT, 0L),
                member("MEM-L002", "会社B", "姓", "名",
                        "REVIEWING", SUBMITTED_AT, 0L),
                member("MEM-L003", "会社C", "姓", "名",
                        "UNREVIEWED", SUBMITTED_AT, 0L),
                member("MEM-L004", "会社D", "姓", "名",
                        "REJECTED", SUBMITTED_AT, 0L)));

        String html = render();

        assertThat(html, matchesPattern(
                "(?s).*<span class=\"badge badge-approved\"\\s*>承認済み</span>.*"));
        assertThat(html, matchesPattern(
                "(?s).*<span class=\"badge badge-reviewing\"\\s*>審査中</span>.*"));
        assertThat(html, matchesPattern(
                "(?s).*<span class=\"badge badge-unreviewed\"\\s*>未審査</span>.*"));
        assertThat(html, matchesPattern(
                "(?s).*<span class=\"badge badge-rejected\"\\s*>否決</span>.*"));
    }

    @Test
    void T19_一覧_添付書類は0件のときなし_1件以上のときN件() throws Exception {
        when(memberListService.findAll()).thenReturn(List.of(
                member("MEM-L001", "会社A", "姓", "名",
                        "APPROVED", SUBMITTED_AT, 3L),
                member("MEM-L003", "会社C", "姓", "名",
                        "UNREVIEWED", SUBMITTED_AT, 0L)));

        String html = render();

        assertThat(html, matchesPattern(
                "(?s).*<td class=\"docs\"[^>]*>なし</td>.*"));
        assertThat(html, matchesPattern(
                "(?s).*<td class=\"docs\"[^>]*>3件</td>.*"));
    }

    @Test
    void T20_一覧_申込日はyyyyMMdd_nullはハイフン() throws Exception {
        when(memberListService.findAll()).thenReturn(List.of(
                member("MEM-L001", "会社A", "姓", "名",
                        "APPROVED", SUBMITTED_AT, 0L),
                member("MEM-L004", "会社D", "姓", "名",
                        "REJECTED", null, 0L)));

        String html = render();

        assertThat(html, matchesPattern(
                "(?s).*<td class=\"date\"\\s*>2026/08/25</td>.*"));
        assertThat(html, matchesPattern(
                "(?s).*<td class=\"date\"\\s*>-</td>.*"));
    }

    @Test
    void T21_一覧_0件でも空状態行がレンダリングされる() throws Exception {
        when(memberListService.findAll()).thenReturn(Collections.emptyList());

        String html = render();

        assertThat(html, containsString("id=\"empty-row\""));
        assertThat(html, containsString("会員データが存在しません。"));
    }

    @Test
    void T22_未認証のとき運用管理ログイン画面へリダイレクトする() throws Exception {
        mockMvc.perform(get("/member_list.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_operation.html"));
    }

    // =========================================================
    // ヘルパー
    // =========================================================

    private String render() throws Exception {
        MvcResult result = mockMvc.perform(get("/member_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    private static MemberListItemDto member(
            String memberCode,
            String corporateName,
            String repLastName,
            String repFirstName,
            String applicationStatus,
            LocalDateTime submittedAt,
            long documentCount) {
        return new MemberListItemDto(
                memberCode,
                corporateName,
                repLastName,
                repFirstName,
                applicationStatus,
                submittedAt,
                documentCount);
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
