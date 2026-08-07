package jp.co.dragonagency.dapaycore.controller;

import jp.co.dragonagency.dapaycore.service.FeeRateService;
import jp.co.dragonagency.dapaycore.service.MemberListService;
import jp.co.dragonagency.dapaycore.service.MerchantApplicationInquiryService;
import jp.co.dragonagency.dapaycore.service.TransferFeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 画面テンプレートを配信するコントローラー。
 * 既存テンプレートは相互のリンクや iframe を「xxx.html」という相対パスで
 * 参照しているため、各画面を「/xxx.html」のパスで配信して遷移を維持する。
 */
@Controller
public class PageController {

    private final MemberListService memberListService;
    private final MerchantApplicationInquiryService inquiryService;
    private final FeeRateService feeRateService;
    private final TransferFeeService transferFeeService;

    public PageController(
            MemberListService memberListService,
            MerchantApplicationInquiryService inquiryService,
            FeeRateService feeRateService,
            TransferFeeService transferFeeService) {
        this.memberListService = memberListService;
        this.inquiryService = inquiryService;
        this.feeRateService = feeRateService;
        this.transferFeeService = transferFeeService;
    }

    /**
     * ルートアクセス時はトップ（index）画面を表示する。
     * index 画面はログイン画面へ自動遷移する。
     */
    @GetMapping({"/", "/index.html"})
    public String showIndex() {
        return "index";
    }

    /**
     * 申込・照会のログイン画面を表示する。
     */
    @GetMapping("/login.html")
    public String showLogin() {
        return "login";
    }

    /**
     * 運用管理のログイン画面を表示する。
     */
    @GetMapping("/login_operation.html")
    public String showOperationLogin() {
        return "login_operation";
    }

    /**
     * 運用管理ポータル（メイン）画面を表示する。
     */
    @GetMapping("/operation_management_portal.html")
    public String showOperationManagementPortal() {
        return "operation_management_portal";
    }

    /**
     * 会員一覧画面を表示する。運用管理ポータルの「加盟店管理」から表示する。
     * m_merchant_application と m_merchant_application_document からデータを取得してモデルへ渡す。
     */
    @GetMapping("/member_list.html")
    public String showMemberList(Model model) {
        model.addAttribute("members", memberListService.findAll());
        return "member_list";
    }

    /**
     * パスワード再設定画面を表示する。
     */
    @GetMapping("/password_reset.html")
    public String showPasswordReset() {
        return "password_reset";
    }

    /**
     * 本パスワード登録画面を表示する。
     * メールのURLからアクセスされる（例: /password-setup.html?code=MA-2026-00001）。
     */
    @GetMapping("/password-setup.html")
    public String showPasswordSetup() {
        return "password_setup";
    }

    /**
     * 申込・照会ポータル（メイン）画面を表示する。
     */
    @GetMapping("/merchant_application_wizard.html")
    public String showMerchantApplicationWizard() {
        return "merchant_application_wizard";
    }

    /**
     * 決済会社申込情報登録画面を表示する。
     */
    @GetMapping("/merchant_application_form.html")
    public String showMerchantApplicationForm() {
        return "merchant_application_form";
    }

    /**
     * 申込内容照会画面を表示する。
     * transactionCode（会員コード）に紐づく申込情報・書類情報を DB から取得してモデルへ渡す。
     */
    @GetMapping("/merchant_application_inquiry.html")
    public String showMerchantApplicationInquiry(
            @RequestParam(required = false) String transactionCode,
            Model model) {
        var merchantApp = inquiryService.findApplication(transactionCode);
        model.addAttribute("merchantApp", merchantApp);
        if (merchantApp != null) {
            model.addAttribute("docMap", inquiryService.findDocumentMap(transactionCode));
        }
        return "merchant_application_inquiry";
    }

    /**
     * 精算情報照会画面を表示する。
     */
    @GetMapping("/settlement_inquiry.html")
    public String showSettlementInquiry() {
        return "settlement_inquiry";
    }

    /**
     * 店舗・端末・加盟店データ登録・更新画面を表示する。
     * 運用管理ポータルの「店舗・端末・加盟店データ登録・更新」から表示する。
     */
    @GetMapping("/operation_shop_data_create.html")
    public String showOperationShopDataCreate() {
        return "operation_shop_data_create";
    }

    /**
     * 店舗・端末・SMCC加盟店番号情報照会画面を表示する。
     * 運用管理ポータルの「店舗・端末・加盟店データ照会」から表示する。
     */
    @GetMapping("/operation_store_terminal.html")
    public String showOperationStoreTerminal() {
        return "operation_store_terminal";
    }

    /**
     * 精算情報照会画面を表示する。
     * 運用管理ポータルの「精算情報照会」→加盟店一覧で明細を選択して表示する。
     */
    @GetMapping("/operation_settlement_inquiry.html")
    public String showOperationSettlementInquiry() {
        return "operation_settlement_inquiry";
    }

    /**
     * 精算データ登録画面を表示する。
     * 運用管理ポータルの「精算データ登録」から表示する。
     */
    @GetMapping("/operation_settlement.html")
    public String showOperationSettlement() {
        return "operation_settlement";
    }

    /**
     * 手数料一覧画面を表示する。
     * 運用管理ポータルの「手数料一覧」から表示する。
     * m_fee_rate と m_merchant_application からデータを取得してモデルへ渡す。
     */
    @GetMapping("/operation_fee_list.html")
    public String showOperationFeeList(Model model) {
        model.addAttribute("feeRates", feeRateService.findAllForList());
        return "operation_fee_list";
    }

    /**
     * 手数料登録・変更画面を表示する。
     * 運用管理ポータルの「手数料登録・変更」または手数料一覧の行クリックから表示する。
     */
    @GetMapping("/operation_fee_edit.html")
    public String showOperationFeeEdit() {
        return "operation_fee_edit";
    }

    /**
     * 振込手数料一覧画面を表示する。
     * 運用管理ポータルの「振込手数料管理」から表示する。
     * m_transfer_fee からデータを取得してモデルへ渡す。
     */
    @GetMapping("/operation_transfer_fee_list.html")
    public String showOperationTransferFeeList(Model model) {
        model.addAttribute("transferFees", transferFeeService.findAllForList());
        return "operation_transfer_fee_list";
    }

    /**
     * 振込手数料登録・変更画面を表示する。
     * 運用管理ポータルの「振込手数料管理」または振込手数料一覧の行クリックから表示する。
     */
    @GetMapping("/operation_transfer_fee_edit.html")
    public String showOperationTransferFeeEdit() {
        return "operation_transfer_fee_edit";
    }

    /**
     * 振込データ作成画面を表示する。
     * 運用管理ポータルの「振込データ作成」から表示する。
     */
    @GetMapping("/operation_transfer.html")
    public String showOperationTransfer() {
        return "operation_transfer";
    }

    /**
     * 振込一覧画面を表示する。
     * マイポータルの「振込一覧」から表示する。
     */
    @GetMapping("/transfer_list.html")
    public String showTransferList() {
        return "transfer_list";
    }

    /**
     * 振込明細画面を表示する。
     * 振込一覧の行クリックから表示する。
     */
    @GetMapping("/transfer_detail.html")
    public String showTransferDetail() {
        return "transfer_detail";
    }

    /**
     * マイポータル向け申込内容照会画面を表示する。
     * セッションの会員コードをもとに申込情報・書類情報を取得してモデルへ渡す。
     *
     * @param session HTTP セッション
     * @param model   画面へ渡すモデル
     * @return マイポータル申込内容照会画面のビュー名
     */
    @GetMapping("/merchant_inquiry.html")
    public String showMerchantInquiry(HttpSession session, Model model) {
        String memberCode = (String) session.getAttribute("merchantMemberCode");
        var merchantApp = inquiryService.findApplication(memberCode);
        model.addAttribute("merchantApp", merchantApp);
        if (merchantApp != null) {
            model.addAttribute("docMap", inquiryService.findDocumentMap(memberCode));
        }
        return "merchant_inquiry";
    }

    /**
     * 機能準備中などを表示する汎用プレースホルダー画面を表示する。
     */
    @GetMapping("/content_placeholder.html")
    public String showContentPlaceholder() {
        return "content_placeholder";
    }
}
