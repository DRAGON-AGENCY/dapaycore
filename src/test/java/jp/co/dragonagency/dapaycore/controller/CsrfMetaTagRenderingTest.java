package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.service.FeeRateService;
import jp.co.dragonagency.dapaycore.service.MemberListService;
import jp.co.dragonagency.dapaycore.service.MerchantApplicationInquiryService;
import jp.co.dragonagency.dapaycore.service.TransferFeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 項番82：CSRF meta タグのレンダリングを検証するテスト。
 *
 * 確認内容:
 *   operation_fee_edit.html において Thymeleaf が
 *   th:content="${csrfToken}" を実際のトークン値に置換し、
 *   レンダリング後の HTML の <meta name="csrf-token" content="..."> に
 *   非空のトークン文字列が埋め込まれること。
 *
 * テスト対象: GET /operation_fee_edit.html
 *   - OperationAuthInterceptor の対象パスのため sessionAttr("loginUser") が必要
 *   - CsrfProtectionInterceptor は GET をスルー
 *   - CsrfTokenControllerAdvice（@ControllerAdvice）がトークンをモデルへ供給
 */
@WebMvcTest(PageController.class)
class CsrfMetaTagRenderingTest {

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

    // =========================================================
    // 項番82：CSRF meta タグに th:content でトークンが埋め込まれる
    // =========================================================

    @Test
    void T82_csrfMetaTag_レンダリングされたHTMLのcontent属性に非空トークンが埋め込まれる()
            throws Exception {
        // th:content="" のフォールバック値（空文字）が残っていたらテスト失敗。
        // Base64URL 40 文字以上の文字列が content に入っていることを確認する。
        mockMvc.perform(get("/operation_fee_edit.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        matchesPattern(
                                "(?s).*<meta name=\"csrf-token\""
                                + " content=\"[A-Za-z0-9_-]{40,}\">.*")));
    }

    @Test
    void T82b_csrfMetaTag_フォールバックの空文字がそのまま出力されていない() throws Exception {
        // content="" のまま残っている場合はトークン未埋め込みを意味する
        mockMvc.perform(get("/operation_fee_edit.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        not(containsString("name=\"csrf-token\" content=\"\""))));
    }

    @Test
    void T82c_csrfMetaTag_異なるセッションでは独立したトークンが生成される() throws Exception {
        // MockMvc はデフォルトでリクエストごとに独立したセッションを使う
        String html1 = mockMvc.perform(get("/operation_fee_edit.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String html2 = mockMvc.perform(get("/operation_fee_edit.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertNotEquals(extractCsrfToken(html1), extractCsrfToken(html2),
                "セッションが異なるのに同じトークンが返されています");
    }

    private static String extractCsrfToken(String html) {
        Matcher m = Pattern
                .compile("name=\"csrf-token\" content=\"([A-Za-z0-9_-]+)\"")
                .matcher(html);
        if (m.find()) {
            return m.group(1);
        }
        throw new AssertionError("csrf-token meta タグが見つかりません");
    }
}
