package jp.co.dragonagency.dapaycore.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.dto.ContactInquiryRequest;
import jp.co.dragonagency.dapaycore.dto.ContactInquiryResponse;
import jp.co.dragonagency.dapaycore.service.ContactInquiryService;

/**
 * お問い合わせの送信要求を処理するコントローラ。
 */
@Controller
public class ContactInquiryController {

    private final ContactInquiryService contactInquiryService;

    public ContactInquiryController(
            ContactInquiryService contactInquiryService) {
        this.contactInquiryService = contactInquiryService;
    }

    /**
     * お問い合わせを送信（新規登録）する。
     * セッションから会員コードを取得して登録に紐付ける。
     *
     * @param request 画面から送信されたお問い合わせ内容
     * @param session 現在の HTTP セッション
     * @return 処理結果
     */
    @PostMapping(
            value = "/contact_inquiry/submit",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ContactInquiryResponse submit(
            @RequestBody ContactInquiryRequest request,
            HttpSession session) {
        String memberCode = (String) session.getAttribute("merchantMemberCode");
        return contactInquiryService.submitInquiry(request, memberCode);
    }
}
