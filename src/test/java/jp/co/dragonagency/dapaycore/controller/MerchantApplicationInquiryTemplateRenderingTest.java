package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.model.MerchantApplicationDocument;
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

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * merchant_application_inquiry.html のテンプレート表示を検証するテスト。
 * 単体テスト仕様書_申込内容照会画面_v1.00.xlsx の項番82〜90に対応する。
 */
@WebMvcTest(PageController.class)
class MerchantApplicationInquiryTemplateRenderingTest {

    private static final String MEMBER_CODE = "TEST0001";

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

    // =========================================================
    // 項番82〜83：not-found の切り替え
    // =========================================================

    @Test
    void T82_merchantAppがnullのとき会員情報が見つかりませんでしたが表示される() throws Exception {
        mockMvc.perform(get("/merchant_application_inquiry.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("会員情報が見つかりませんでした。会員一覧から再度お選びください。")));
    }

    @Test
    void T83_merchantAppが非nullのときnotFoundが非表示でコンテンツエリアが表示される() throws Exception {
        when(inquiryService.findApplication(MEMBER_CODE)).thenReturn(buildApplication());
        when(inquiryService.findDocumentMap(MEMBER_CODE)).thenReturn(Map.of());

        mockMvc.perform(get("/merchant_application_inquiry.html")
                        .param("transactionCode", MEMBER_CODE)
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        not(containsString("会員情報が見つかりませんでした。会員一覧から再度お選びください。"))))
                .andExpect(content().string(
                        containsString("id=\"hd-memberCode\"")));
    }

    // =========================================================
    // 項番84〜87：ステータス表示
    // =========================================================

    @Test
    void T84_applicationStatusがUNREVIEWEDのとき未審査のステータスが表示される() throws Exception {
        assertStatusLabel(MerchantApplication.STATUS_UNREVIEWED, "未審査");
    }

    @Test
    void T85_applicationStatusがREVIEWINGのとき審査中のステータスが表示される() throws Exception {
        assertStatusLabel(MerchantApplication.STATUS_REVIEWING, "審査中");
    }

    @Test
    void T86_applicationStatusがAPPROVEDのとき承認済みのステータスが表示される() throws Exception {
        assertStatusLabel(MerchantApplication.STATUS_APPROVED, "承認済み");
    }

    @Test
    void T87_applicationStatusがREJECTEDのとき否認のステータスが表示される() throws Exception {
        assertStatusLabel(MerchantApplication.STATUS_REJECTED, "否認");
    }

    private void assertStatusLabel(String status, String expectedLabel) throws Exception {
        MerchantApplication app = buildApplication();
        app.setApplicationStatus(status);
        when(inquiryService.findApplication(MEMBER_CODE)).thenReturn(app);
        when(inquiryService.findDocumentMap(MEMBER_CODE)).thenReturn(Map.of());

        mockMvc.perform(get("/merchant_application_inquiry.html")
                        .param("transactionCode", MEMBER_CODE)
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedLabel)));
    }

    // =========================================================
    // 項番88〜89：書類提出状況の表示
    // =========================================================

    @Test
    void T88_docMapのBUSINESS_PERMITが存在するとき提出済みとダウンロードボタンが表示される() throws Exception {
        when(inquiryService.findApplication(MEMBER_CODE)).thenReturn(buildApplication());
        when(inquiryService.findDocumentMap(MEMBER_CODE)).thenReturn(
                Map.of(MerchantApplicationDocument.TYPE_BUSINESS_PERMIT, buildDocument()));

        mockMvc.perform(get("/merchant_application_inquiry.html")
                        .param("transactionCode", MEMBER_CODE)
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("提出済み")))
                .andExpect(content().string(containsString("ダウンロード")));
    }

    @Test
    void T89_docMapのBUSINESS_PERMITが存在しないとき未提出が表示される() throws Exception {
        when(inquiryService.findApplication(MEMBER_CODE)).thenReturn(buildApplication());
        when(inquiryService.findDocumentMap(MEMBER_CODE)).thenReturn(Map.of());

        mockMvc.perform(get("/merchant_application_inquiry.html")
                        .param("transactionCode", MEMBER_CODE)
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("未提出")));
    }

    // =========================================================
    // 項番90：CSRF meta タグ
    // =========================================================

    @Test
    void T90_csrfMetaTagにthContentでトークンが埋め込まれる() throws Exception {
        mockMvc.perform(get("/merchant_application_inquiry.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        matchesPattern(
                                "(?s).*<meta name=\"csrf-token\""
                                + " content=\"[A-Za-z0-9_-]{40,}\">.*")));
    }

    private static MerchantApplication buildApplication() {
        MerchantApplication app = new MerchantApplication();
        app.setMemberCode(MEMBER_CODE);
        app.setApplicationStatus(MerchantApplication.STATUS_UNREVIEWED);
        return app;
    }

    private static MerchantApplicationDocument buildDocument() {
        MerchantApplicationDocument doc = new MerchantApplicationDocument();
        doc.setMemberCode(MEMBER_CODE);
        doc.setDocumentType(MerchantApplicationDocument.TYPE_BUSINESS_PERMIT);
        doc.setFileName("permit.pdf");
        return doc;
    }
}
