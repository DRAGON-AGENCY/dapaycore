package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.NetStarsImportControlView;
import jp.co.dragonagency.dapaycore.dto.NetStarsImportHistoryListItemDto;
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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * operation_netstars_import.html の Web 層（レンダリング）テスト。
 * 単体テスト仕様書_還元データ取込履歴照会_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T49〜T56（■ テストケース一覧）に対応する。
 * INPUT データ: C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_還元データ取込履歴照会_v1.00.xlsx
 */
@WebMvcTest(PageController.class)
class NetStarsImportTemplateRenderingTest {

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

    private static final NetStarsImportControlView RUNNING_VIEW =
            new NetStarsImportControlView(true, true, false, "", "");

    @Test
    void T49_履歴_件数分の行がレンダリングされる() throws Exception {
        when(netStarsSettlementImportService.findHistoryForList()).thenReturn(List.of(
                history("2026/09/01 10:00:00", "自動", "SUCCESS", "成功"),
                history("2026/08/31 10:00:03", "自動", "SUCCESS", "成功"),
                history("2026/08/30 10:00:01", "自動再開", "FAILED", "失敗")));
        when(netStarsSettlementImportService.getControlView()).thenReturn(RUNNING_VIEW);

        String html = perform("user001");

        assertContains(html, "2026/09/01 10:00:00");
        assertContains(html, "2026/08/31 10:00:03");
        assertContains(html, "2026/08/30 10:00:01");
    }

    @Test
    void T50_履歴_状態ごとにバッジが描画される() throws Exception {
        when(netStarsSettlementImportService.findHistoryForList()).thenReturn(List.of(
                history("2026/09/01 10:00:00", "自動", "SUCCESS", "成功"),
                history("2026/09/02 10:00:00", "自動", "FAILED", "失敗"),
                history("2026/09/03 10:00:00", "自動", "SKIPPED", "スキップ"),
                history("2026/09/04 10:00:00", "自動", "RUNNING", "実行中")));
        when(netStarsSettlementImportService.getControlView()).thenReturn(RUNNING_VIEW);

        mockMvc.perform(get("/operation_netstars_import.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("成功")))
                .andExpect(content().string(containsString("失敗")))
                .andExpect(content().string(containsString("スキップ")))
                .andExpect(content().string(containsString("実行中")));
    }

    @Test
    void T51_履歴_種別ラベルが描画される() throws Exception {
        when(netStarsSettlementImportService.findHistoryForList()).thenReturn(List.of(
                history("2026/09/01 10:00:00", "自動", "SUCCESS", "成功"),
                history("2026/09/02 09:00:00", "手動再開", "SUCCESS", "成功"),
                history("2026/09/03 10:00:00", "自動再開", "SUCCESS", "成功")));
        when(netStarsSettlementImportService.getControlView()).thenReturn(RUNNING_VIEW);

        mockMvc.perform(get("/operation_netstars_import.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("手動再開")))
                .andExpect(content().string(containsString("自動再開")));
    }

    @Test
    void T52_履歴_0件でも空状態行がレンダリングされる() throws Exception {
        when(netStarsSettlementImportService.findHistoryForList())
                .thenReturn(Collections.emptyList());
        when(netStarsSettlementImportService.getControlView()).thenReturn(RUNNING_VIEW);

        mockMvc.perform(get("/operation_netstars_import.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("取込履歴はありません。")));
    }

    @Test
    void T53_受信状態_稼働中のとき稼働中バッジとSTOPボタンが描画される() throws Exception {
        when(netStarsSettlementImportService.findHistoryForList())
                .thenReturn(Collections.emptyList());
        when(netStarsSettlementImportService.getControlView()).thenReturn(RUNNING_VIEW);

        String html = perform("user001");

        assertContains(html, "稼働中");
        assertContains(html, "受信を停止（STOP）");
    }

    @Test
    void T54_受信状態_停止中のとき停止中バッジとRESTARTボタンと停止日時が描画される()
            throws Exception {
        when(netStarsSettlementImportService.findHistoryForList())
                .thenReturn(Collections.emptyList());
        when(netStarsSettlementImportService.getControlView()).thenReturn(
                new NetStarsImportControlView(false, false, false,
                        "2026/09/01 10:00", "2026/09/06"));

        String html = perform("user001");

        assertContains(html, "停止中");
        assertContains(html, "受信を再開（RESTART）");
        assertContains(html, "2026/09/01 10:00 に停止しました。");
        assertContains(html, "2026/09/06 頃に自動で再開します。");
    }

    @Test
    void T55_csrfMetaTag_content属性に非空トークンが埋め込まれる() throws Exception {
        when(netStarsSettlementImportService.findHistoryForList())
                .thenReturn(Collections.emptyList());
        when(netStarsSettlementImportService.getControlView()).thenReturn(RUNNING_VIEW);

        mockMvc.perform(get("/operation_netstars_import.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern(
                        "(?s).*<meta name=\"csrf-token\""
                        + " content=\"[A-Za-z0-9_-]{40,}\">.*")));
    }

    @Test
    void T56_未認証のとき運用管理ログイン画面へリダイレクトする() throws Exception {
        mockMvc.perform(get("/operation_netstars_import.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_operation.html"));
    }

    private String perform(String loginUser) throws Exception {
        return mockMvc.perform(get("/operation_netstars_import.html")
                        .sessionAttr("loginUser", loginUser))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private static void assertContains(String html, String needle) {
        org.junit.jupiter.api.Assertions.assertTrue(
                html.contains(needle),
                "レンダリング結果に \"" + needle + "\" が含まれていません。");
    }

    private static NetStarsImportHistoryListItemDto history(
            String startedAt, String typeLabel, String statusCode, String statusLabel) {
        NetStarsImportHistoryListItemDto d = new NetStarsImportHistoryListItemDto();
        d.setId(1L);
        d.setStartedAt(startedAt);
        d.setImportTypeLabel(typeLabel);
        d.setStatusCode(statusCode);
        d.setStatusLabel(statusLabel);
        d.setTargetPeriod("2026/09/01");
        d.setFetchedCount(0);
        d.setInsertedCount(0);
        d.setUpdatedCount(0);
        d.setFinishedAt(startedAt);
        d.setErrorMessage("");
        return d;
    }
}
