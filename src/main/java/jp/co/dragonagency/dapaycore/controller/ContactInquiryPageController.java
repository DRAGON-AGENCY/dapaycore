package jp.co.dragonagency.dapaycore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.co.dragonagency.dapaycore.service.ContactInquiryService;

/**
 * お問い合わせ画面を表示するコントローラ。
 * 過去のお問い合わせ履歴を一覧として画面に供給する。
 */
@Controller
public class ContactInquiryPageController {

    private static final String ATTRIBUTE_INQUIRIES = "inquiries";
    private static final String VIEW_CONTACT_INQUIRY = "contact_inquiry";

    private final ContactInquiryService contactInquiryService;

    public ContactInquiryPageController(
            ContactInquiryService contactInquiryService) {
        this.contactInquiryService = contactInquiryService;
    }

    /**
     * お問い合わせ画面を表示する。受付日時の降順で履歴一覧を渡す。
     *
     * @param model 画面へ渡すモデル
     * @return お問い合わせ画面のビュー名
     */
    @GetMapping("/contact_inquiry.html")
    public String contactInquiry(Model model) {
        model.addAttribute(
                ATTRIBUTE_INQUIRIES,
                contactInquiryService.findAllInquiries());
        return VIEW_CONTACT_INQUIRY;
    }
}
