package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.dto.ContactInquiryListItemDto;
import jp.co.dragonagency.dapaycore.model.ContactInquiry;
import jp.co.dragonagency.dapaycore.service.ContactInquiryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * {@link ContactInquiryPageController} の Web 層テスト。
 * 単体テスト仕様書_お問い合わせ履歴_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T11〜T19（■ テストケース一覧）に対応する。
 * INPUT データ: C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_お問い合わせ履歴_v1.00.xlsx
 *
 * <p>{@code /operation_contact_inquiry_list.html} は OperationAuthInterceptor の
 * 対象パスのため、認証済みのテストでは {@code sessionAttr("loginUser", ...)} を付与する。</p>
 */
@WebMvcTest(ContactInquiryPageController.class)
class ContactInquiryPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactInquiryService contactInquiryService;

    @Test
    void T11_operationContactInquiryList_inquiriesにサービスの戻り値が設定される()
            throws Exception {
        List<ContactInquiryListItemDto> expected = List.of(
                dto("INQ-0001", "MEM-0001", "株式会社テスト商事",
                        ContactInquiry.STATUS_RECEIVED));
        when(contactInquiryService.findAllInquiriesForOperation())
                .thenReturn(expected);

        mockMvc.perform(get("/operation_contact_inquiry_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("inquiries", expected));
    }

    @Test
    void T12_operationContactInquiryList_データなしのときinquiriesが空リスト()
            throws Exception {
        when(contactInquiryService.findAllInquiriesForOperation())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/operation_contact_inquiry_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("inquiries", Collections.emptyList()));
    }

    @Test
    void T13_operationContactInquiryList_ビュー名operation_contact_inquiry_listを返す()
            throws Exception {
        when(contactInquiryService.findAllInquiriesForOperation())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/operation_contact_inquiry_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(view().name("operation_contact_inquiry_list"));
    }

    @Test
    void T14_一覧_inquiriesの件数分のdataRowがレンダリングされる() throws Exception {
        when(contactInquiryService.findAllInquiriesForOperation())
                .thenReturn(List.of(
                        dto("INQ-0001", "MEM-0001", "エーシャ",
                                ContactInquiry.STATUS_RECEIVED),
                        dto("INQ-0002", "MEM-0002", "ビーシャ",
                                ContactInquiry.STATUS_IN_PROGRESS),
                        dto("INQ-0003", "", "",
                                ContactInquiry.STATUS_ANSWERED)));

        String html = mockMvc.perform(get("/operation_contact_inquiry_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertContains(html, "INQ-0001");
        assertContains(html, "INQ-0002");
        assertContains(html, "INQ-0003");
    }

    @Test
    void T15_一覧_ステータスごとに対応するバッジが描画される() throws Exception {
        when(contactInquiryService.findAllInquiriesForOperation())
                .thenReturn(List.of(
                        dto("INQ-0001", "", "", ContactInquiry.STATUS_RECEIVED),
                        dto("INQ-0002", "", "", ContactInquiry.STATUS_IN_PROGRESS),
                        dto("INQ-0003", "", "", ContactInquiry.STATUS_ANSWERED),
                        dto("INQ-0004", "", "", ContactInquiry.STATUS_CLOSED)));

        mockMvc.perform(get("/operation_contact_inquiry_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("受付中")))
                .andExpect(content().string(containsString("対応中")))
                .andExpect(content().string(containsString("回答済み")))
                .andExpect(content().string(containsString("クローズ")));
    }

    @Test
    void T16_一覧_0件でも空状態行がレンダリングされる() throws Exception {
        when(contactInquiryService.findAllInquiriesForOperation())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/operation_contact_inquiry_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("該当するお問い合わせがありません。")));
    }

    @Test
    void T17_一覧_各行に詳細モーダル用のdata属性が埋め込まれる() throws Exception {
        ContactInquiryListItemDto item =
                dto("INQ-0001", "MEM-0001", "テストショウジ",
                        ContactInquiry.STATUS_RECEIVED);
        item.setCategory("請求・精算について");
        item.setSubject("決済手数料の内訳について");
        item.setBody("計算根拠を教えてください。");
        when(contactInquiryService.findAllInquiriesForOperation())
                .thenReturn(List.of(item));

        String html = mockMvc.perform(get("/operation_contact_inquiry_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertContains(html, "data-no=\"INQ-0001\"");
        assertContains(html, "data-code=\"MEM-0001\"");
        assertContains(html, "data-kana=\"テストショウジ\"");
        assertContains(html, "data-cat=\"請求・精算について\"");
        assertContains(html, "data-subj=\"決済手数料の内訳について\"");
        assertContains(html, "data-status=\"RECEIVED\"");
        assertContains(html, "data-body=\"計算根拠を教えてください。\"");
    }

    @Test
    void T18_csrfMetaTag_content属性に非空トークンが埋め込まれる() throws Exception {
        when(contactInquiryService.findAllInquiriesForOperation())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/operation_contact_inquiry_list.html")
                        .sessionAttr("loginUser", "user001"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern(
                        "(?s).*<meta name=\"csrf-token\""
                        + " content=\"[A-Za-z0-9_-]{40,}\">.*")));
    }

    @Test
    void T19_未認証のとき運用管理ログイン画面へリダイレクトする() throws Exception {
        mockMvc.perform(get("/operation_contact_inquiry_list.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_operation.html"));
    }

    private static void assertContains(String html, String needle) {
        org.junit.jupiter.api.Assertions.assertTrue(
                html.contains(needle),
                "レンダリング結果に \"" + needle + "\" が含まれていません。");
    }

    private static ContactInquiryListItemDto dto(
            String number, String memberCode, String kana, String status) {
        ContactInquiryListItemDto d = new ContactInquiryListItemDto();
        d.setInquiryNumber(number);
        d.setMemberCode(memberCode);
        d.setCorporateNameKana(kana);
        d.setCategory("その他");
        d.setSubject("件名 " + number);
        d.setBody("本文 " + number);
        d.setStatus(status);
        d.setCreatedAt(LocalDateTime.of(2026, 8, 2, 10, 0));
        d.setUpdatedAt(LocalDateTime.of(2026, 8, 2, 12, 0));
        return d;
    }
}
