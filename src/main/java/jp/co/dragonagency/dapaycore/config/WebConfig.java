package jp.co.dragonagency.dapaycore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jp.co.dragonagency.dapaycore.interceptor.CsrfProtectionInterceptor;
import jp.co.dragonagency.dapaycore.interceptor.OperationAuthInterceptor;

/**
 * Spring MVC の追加設定。
 * CSRF 検査インターセプターと運用管理認証インターセプターを登録する。
 * 静的リソース・favicon・error は CSRF 検査対象外とする。
 * 運用管理認証インターセプターは運用管理ポータルのパスにのみ適用する。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String PATTERN_ALL = "/**";
    private static final String PATTERN_STATIC_CSS = "/css/**";
    private static final String PATTERN_STATIC_JS = "/js/**";
    private static final String PATTERN_STATIC_IMAGES = "/images/**";
    private static final String PATTERN_FAVICON = "/favicon.ico";
    private static final String PATTERN_ERROR = "/error";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 参照系メソッドはインターセプター内で検査せず通過させるため、
        // ログイン画面・ルートを対象から除外する必要はない。
        registry.addInterceptor(new CsrfProtectionInterceptor())
                .addPathPatterns(PATTERN_ALL)
                .excludePathPatterns(
                        PATTERN_STATIC_CSS,
                        PATTERN_STATIC_JS,
                        PATTERN_STATIC_IMAGES,
                        PATTERN_FAVICON,
                        PATTERN_ERROR);

        registry.addInterceptor(new OperationAuthInterceptor())
                .addPathPatterns(
                        "/operation_management_portal.html",
                        "/member_list.html",
                        "/merchant_application_inquiry.html",
                        "/employee_list.html",
                        "/employee_edit.html",
                        "/employee/save",
                        "/employee/delete",
                        "/api/merchant-application-inquiry/update",
                        "/api/merchant-application-inquiry/delete",
                        "/operation_shop_data_create.html",
                        "/operation_store_terminal.html",
                        "/operation_settlement_inquiry.html",
                        "/operation_settlement.html",
                        "/operation_fee_list.html",
                        "/operation_fee_edit.html",
                        "/api/fee",
                        "/api/fee/**",
                        "/api/member/*/name",
                        "/operation_transfer.html",
                        "/content_placeholder.html");
    }
}
