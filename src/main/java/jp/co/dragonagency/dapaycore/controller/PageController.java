package jp.co.dragonagency.dapaycore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 画面テンプレートを配信するコントローラー。
 * 既存テンプレートは相互のリンクや iframe を「xxx.html」という相対パスで
 * 参照しているため、各画面を「/xxx.html」のパスで配信して遷移を維持する。
 */
@Controller
public class PageController {

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
     * パスワード再設定画面を表示する。
     */
    @GetMapping("/password_reset.html")
    public String showPasswordReset() {
        return "password_reset";
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
     * 精算情報照会画面を表示する。
     */
    @GetMapping("/settlement_inquiry.html")
    public String showSettlementInquiry() {
        return "settlement_inquiry";
    }

    /**
     * 機能準備中などを表示する汎用プレースホルダー画面を表示する。
     */
    @GetMapping("/content_placeholder.html")
    public String showContentPlaceholder() {
        return "content_placeholder";
    }
}
