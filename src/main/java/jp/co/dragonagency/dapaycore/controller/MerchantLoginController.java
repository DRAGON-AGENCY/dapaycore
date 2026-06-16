package jp.co.dragonagency.dapaycore.controller;

import jakarta.servlet.http.HttpSession;
import jp.co.dragonagency.dapaycore.dto.MerchantLoginRequest;
import jp.co.dragonagency.dapaycore.dto.MerchantLoginResponse;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.MerchantLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 加盟店ログイン API を提供するコントローラ。
 */
@RestController
@RequestMapping("/api/merchant")
public class MerchantLoginController {

    private static final Logger log = LoggerFactory.getLogger(MerchantLoginController.class);

    private final MerchantLoginService merchantLoginService;

    public MerchantLoginController(MerchantLoginService merchantLoginService) {
        this.merchantLoginService = merchantLoginService;
    }

    /**
     * 加盟店ログインを処理する。
     * 認証成功時にセッションへ会員コードを保持する。
     * 仮パスワードでの認証成功は requiresPasswordSetup=true で返す。
     */
    @PostMapping("/login")
    public ResponseEntity<MerchantLoginResponse> login(
            @RequestBody MerchantLoginRequest request,
            HttpSession session) {
        MerchantLoginResponse response = merchantLoginService.authenticate(
                request.getEmail(), request.getPassword());
        if (response.isSuccess()) {
            session.setAttribute(
                    SessionAttributeNames.MERCHANT_MEMBER_CODE,
                    response.getMemberCode());
            log.info("加盟店ログイン成功: memberCode={}", response.getMemberCode());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
